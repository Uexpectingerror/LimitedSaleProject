package com.orsonwu.limited_sale;


import com.orsonwu.limited_sale.db.dao.LimitedSaleEventDao;
import com.orsonwu.limited_sale.db.mappers.LimitedSaleEventMapper;
import com.orsonwu.limited_sale.db.po.LimitedSaleEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class DaoTest {
    @Resource
    private LimitedSaleEventMapper limitedSaleEventMapper;
    @Autowired
    private LimitedSaleEventDao limitedSaleEventDao;

    @Test
    void LimitedSaleEventTest() {
        LimitedSaleEvent saleEvent = new LimitedSaleEvent();
        saleEvent.setEventName("TestEvent4");
        saleEvent.setCommodityId(999L);
        saleEvent.setTotalStock(100L);
        saleEvent.setSalePrice(new BigDecimal(99));
        saleEvent.setEventStatus(0);
        saleEvent.setOriginalPrice(new BigDecimal(99));
        saleEvent.setAvailableStock(100L);
        saleEvent.setLockStock(0L);
        limitedSaleEventMapper.insert(saleEvent);
        System.out.println("orsonwu dao test: \n");
        System.out.println("====>>>>" + limitedSaleEventMapper.selectByPrimaryKey(1L));
    }

    @Test
    void setLimitedSaleEventQuery() {
        List<LimitedSaleEvent> saleEvents = limitedSaleEventDao.queryLimitedSaleEventsByStatus(0);
        System.out.println(saleEvents.size());
        saleEvents.stream().forEach(saleEvent -> System.out.println(saleEvent.getEventName()));
    }
}
