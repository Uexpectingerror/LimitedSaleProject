package com.orsonwu.limited_sale.db.dao;

import com.orsonwu.limited_sale.db.mappers.LimitedSaleEventMapper;
import com.orsonwu.limited_sale.db.po.LimitedSaleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class LimitedSaleEventDaoImpl implements LimitedSaleEventDao {

    @Resource
    private LimitedSaleEventMapper limitedSaleEventMapper;

    @Override
    public List<LimitedSaleEvent> queryLimitedSaleEventsByStatus(int eventStatus) {
        return limitedSaleEventMapper.queryLimitedSaleEventsByStatus(eventStatus);
    }

    @Override
    public void insertLimitedSaleEvent(LimitedSaleEvent saleEvent) {
        limitedSaleEventMapper.insert(saleEvent);
    }

    @Override
    public LimitedSaleEvent queryLimitedSaleEventById(long eventId) {
        return limitedSaleEventMapper.selectByPrimaryKey(eventId);
    }

    @Override
    public void updateLimitedSaleEvent(LimitedSaleEvent saleEvent) {
        limitedSaleEventMapper.updateByPrimaryKey(saleEvent);
    }

    @Override
    public boolean deductStock(long eventId) {
        int result = limitedSaleEventMapper.deductStock(eventId);
        if (result < 1) {
            log.error("deduct stock failed");
            return false;
        }
        return true;
    }

    @Override
    public boolean lockStock(long eventId) {
        int result = limitedSaleEventMapper.lockStock(eventId);
        if (result < 1) {
            log.error("lock stock failed");
            return false;
        }
        return true;
    }

    @Override
    public boolean revertStock(long eventId) {
        return limitedSaleEventMapper.revertStock(eventId);
    }
}
