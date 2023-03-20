package com.orsonwu.limited_sale.mq;

import com.alibaba.fastjson.JSON;
import com.orsonwu.limited_sale.db.dao.LimitedSaleEventDao;
import com.orsonwu.limited_sale.db.dao.LimitedSaleOrderDao;
import com.orsonwu.limited_sale.db.po.LimitedSaleOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * process order complete messages
 * deduct stock in the data base
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_done", consumerGroup = "pay_done_group")
public class PayDoneConsumer implements RocketMQListener<MessageExt> {
    @Autowired
    private LimitedSaleOrderDao orderDao;

    @Autowired
    private LimitedSaleEventDao eventDao;

    @Override
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("receive order payment completed message：" + message);
        LimitedSaleOrder order = JSON.parseObject(message, LimitedSaleOrder.class);
        //2.Deduct the data base
        eventDao.deductStock(order.getEventId());
    }
}
