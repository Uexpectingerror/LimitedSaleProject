package com.orsonwu.limited_sale.component;

import com.orsonwu.limited_sale.db.dao.LimitedSaleEventDao;
import com.orsonwu.limited_sale.db.po.LimitedSaleEvent;
import com.orsonwu.limited_sale.services.LimitedSaleEventService;
import com.orsonwu.limited_sale.util.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisPreheatRunner implements ApplicationRunner {
    @Autowired
    RedisService redisService;

    @Autowired
    LimitedSaleEventDao limitedSaleEventDao;

    @Autowired
    LimitedSaleEventService limitedSaleEventService;
    /**
     * when the application starts, insert sql data into the redis
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<LimitedSaleEvent> saleEvents = limitedSaleEventDao.queryLimitedSaleEventsByStatus(1);
        for (LimitedSaleEvent event : saleEvents)
        {
            redisService.setValue("stock:" + event.getEventId(),
                    (long) event.getAvailableStock());
            limitedSaleEventService.pushSaleEventInfoToRedis(event.getEventId());
        }
        System.out.println("Preheated Redis with stock data!");
    }
}
