package com.orsonwu.limited_sale.services;

import com.alibaba.fastjson.JSON;
import com.orsonwu.limited_sale.db.dao.LimitedSaleCommodityDao;
import com.orsonwu.limited_sale.db.dao.LimitedSaleEventDao;
import com.orsonwu.limited_sale.db.dao.LimitedSaleOrderDao;
import com.orsonwu.limited_sale.db.po.LimitedSaleCommodity;
import com.orsonwu.limited_sale.db.po.LimitedSaleEvent;
import com.orsonwu.limited_sale.db.po.LimitedSaleOrder;
import com.orsonwu.limited_sale.mq.RocketMQService;
import com.orsonwu.limited_sale.util.RedisService;
import com.orsonwu.limited_sale.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class LimitedSaleEventService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private LimitedSaleEventDao limitedSaleEventDao;

    @Autowired
    private LimitedSaleCommodityDao limitedSaleCommodityDao;

    @Autowired
    private RocketMQService rocketMQService;

    @Autowired
    private LimitedSaleOrderDao orderDao;

    /**
     * datacenterId;
     * machineId;
     * can be read from machine property in a distributed system
     * here will be hard coded since there is only one machine
     */
    private SnowFlake snowFlake = new SnowFlake(1, 1);

    /**
     * check if the sell event still have stock left
     * @param eventId event id
     * @return
     */
    public boolean limitedSaleStockValidator(long eventId) {
        String key = "stock:" + eventId;
        return redisService.stockDeductValidator(key);
    }
    /**
     * 创建订单
     *
     * @param saleEventId
     * @param userId
     * @return
     * @throws Exception
     */
    public LimitedSaleOrder createOrder(long saleEventId, long userId) throws Exception {
        /*
         * 1.create the order
         */
        LimitedSaleEvent saleEvent = limitedSaleEventDao.queryLimitedSaleEventById(saleEventId);
        LimitedSaleOrder order = new LimitedSaleOrder();
        //Using snowflake to generate id
        order.setOrderNum(String.valueOf(snowFlake.nextId()));
        order.setEventId(saleEvent.getEventId());
        order.setUserId(userId);
        order.setPayAmount(saleEvent.getSalePrice().longValue());
        /*
         *2.Send the order info as json to the MQ
         */
        rocketMQService.sendMessage("limitedsale_order", JSON.toJSONString(order));

        /*
         * 3.Sned delayed message to check if the order is completed by then
         * messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
         */
        rocketMQService.sendDelayMessage("pay_check",JSON.toJSONString(order),5);
        return order;
    }

    /**
     * check if the sell event still have stock left
     * @param eventId event id
     * @return
     */
    public boolean isUserRestrainedFromEvent(long eventId, long userId) {
        return redisService.isInLimitMember(eventId, userId);
    }

    /**
     * process finished oder with payment
     * @param orderNum
     */
    public void payOrderProcess(String orderNum) throws Exception {
        log.info("Finished the order payment Order Number：" + orderNum);
        LimitedSaleOrder order = orderDao.queryOrder(orderNum);

        if (order == null) {
            log.error("order can't be found：" + orderNum);
            return;
        } else if(order.getOrderStatus() != 1 ) {
            log.error("invalid order status：" + orderNum);
            return;
        }

        /*
         * 2.set the pay time
         */
        order.setPayTime(new Date());
        //2 means the order is completed
        order.setOrderStatus((byte) 2);
        orderDao.updateOrder(order);
        /*
         * 3.send order complete message to the MQ and let the receiver to change data base
         */
        rocketMQService.sendMessage("pay_done", JSON.toJSONString(order));
    }

    /**
     * feed in event info into the redis
     *
     * @param saleEventId
     */
    public void pushSaleEventInfoToRedis(long saleEventId) {
        LimitedSaleEvent saleEvent = limitedSaleEventDao.queryLimitedSaleEventById(saleEventId);
        redisService.setValueInHash("ValidSaleEvent", "EventId:" + saleEventId, JSON.toJSONString(saleEvent));

        LimitedSaleCommodity saleCommodity = limitedSaleCommodityDao.queryLimitedSaleCommodityById(saleEvent.getCommodityId());
        redisService.setValueInHash("ValidSaleCommodity","CommodityId:" + saleEvent.getCommodityId(), JSON.toJSONString(saleCommodity));
    }

    /**
     * get all valid events from redis could be an empty list
     * @return
     */
    public List<LimitedSaleEvent> getAllValidEventsFromRedis(){
        List<LimitedSaleEvent> retval = new ArrayList<LimitedSaleEvent>();
        List<String> eventsInfo = redisService.getAllValuesInHash("ValidSaleEvent");
        for (String s:eventsInfo) {
            LimitedSaleEvent temp = JSON.parseObject(s, LimitedSaleEvent.class);
            String sstock = redisService.getValue("stock:"+temp.getEventId());
            long stock = Long.valueOf(sstock);
            temp.setAvailableStock(stock);
            retval.add(temp);
        }
        return  retval;
    }

    /**
     * get all valid commodities from redis could be an empty list
     * @return
     */
    public List<LimitedSaleCommodity> getAllValidCommoditiesFromRedis(){
        List<LimitedSaleCommodity> retval = new ArrayList<LimitedSaleCommodity>();
        List<String> eventsInfo = redisService.getAllValuesInHash("ValidSaleCommodity");
        for (String s:eventsInfo) {
            retval.add(JSON.parseObject(s, LimitedSaleCommodity.class));
        }
        return  retval;
    }

    /**
     * get the target valid event from redis (using redis hash)
     * @return
     */
    public LimitedSaleEvent getValidEventFromRedis(long eventId){
        String eventInfo = redisService.getValueInHash("ValidSaleEvent", "EventId:"+eventId);
        if(eventInfo == "nil"){
            return null;
        }
        LimitedSaleEvent retval = JSON.parseObject(eventInfo, LimitedSaleEvent.class);
        return  retval;
    }

    /**
     * get the target valid event from redis (using redis hash)
     * @return
     */
    public LimitedSaleCommodity getValidCommodityFromRedis(long commodityId){
        String commodityInfo = redisService.getValueInHash("ValidSaleCommodity", "CommodityId:"+commodityId);
        if(commodityInfo == "nil"){
            return null;
        }
        LimitedSaleCommodity retval = JSON.parseObject(commodityInfo, LimitedSaleCommodity.class);
        return  retval;
    }

    public void pushAllEventAndCommodityToRedis(){
        List<LimitedSaleEvent> saleEvents = limitedSaleEventDao.queryLimitedSaleEventsByStatus(1);
        for (LimitedSaleEvent event : saleEvents)
        {
            pushSaleEventInfoToRedis(event.getEventId());
        }
    }
}
