package com.orsonwu.limited_sale.mq;

import com.alibaba.fastjson.JSON;
import com.orsonwu.limited_sale.db.dao.LimitedSaleEventDao;
import com.orsonwu.limited_sale.db.dao.LimitedSaleOrderDao;
import com.orsonwu.limited_sale.db.po.LimitedSaleOrder;
import com.orsonwu.limited_sale.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
//Specify the message topic and consumer group this belongs to
@RocketMQMessageListener(topic = "limitedsale_order", consumerGroup = "limitedsale_order_group")
public class OrderConsumer implements RocketMQListener<MessageExt> {
    @Autowired
    private LimitedSaleOrderDao orderDao;

    @Autowired
    private LimitedSaleEventDao limitedSaleEventDao;
    @Autowired
    RedisService redisService;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        //1.Process order creation request
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("receive order creation request：" + message);

        //convert the json body back to order class
        LimitedSaleOrder order = JSON.parseObject(message, LimitedSaleOrder.class);
        order.setCreateTime(new Date());
        //2.lock the stock first
        boolean lockStockResult = limitedSaleEventDao.lockStock(order.getEventId());
        if (lockStockResult) {
            //oder status 0:out of stock/invalid 1:Created waiting for payment
            order.setOrderStatus((byte) 1);
            //Add this user to the restrained user list from this event, since his order got through
            redisService.addLimitMember(order.getEventId(), order.getUserId());
        } else {
            order.setOrderStatus((byte) 0);
        }
        //3.insert the order
        orderDao.insertOrder(order);
    }
}
