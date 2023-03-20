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

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_check", consumerGroup = "pay_check_group")
//Check if the payment is being made by the user yet
public class PayStatusCheckListener implements RocketMQListener<MessageExt> {
    @Autowired
    private LimitedSaleOrderDao orderDao;

    @Autowired
    private LimitedSaleEventDao eventDao;

    @Resource
    private RedisService redisService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Receive Order Payment Status Check Message: " + message);
        LimitedSaleOrder order = JSON.parseObject(message, LimitedSaleOrder.class);
        //1.look up the order
        LimitedSaleOrder orderInfo = orderDao.queryOrder(order.getOrderNum());
        if (orderInfo == null){
            log.info("Order doesn't exist: " + message);
        }
        //2.Check if the order is finished, IF NOT:
        if (orderInfo.getOrderStatus() != 2) {
            //3.Close the order if it is not paid
            log.info("Closing unpaid overtime order,order number："+orderInfo.getOrderNum());
            //4.update order status
            orderInfo.setOrderStatus((byte) 99);
            orderDao.updateOrder(orderInfo);
            //5. revert the stock deduction in the data base
            eventDao.revertStock(order.getEventId());
            //6. revert the stock dedection on redis
            redisService.revertStock("stock:"+order.getEventId());
            //7. remove user from the restrained list
            redisService.removeLimitMember(order.getEventId(), order.getUserId());
        }
    }
}
