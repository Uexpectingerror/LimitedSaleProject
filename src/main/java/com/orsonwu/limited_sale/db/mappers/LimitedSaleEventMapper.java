package com.orsonwu.limited_sale.db.mappers;

import com.orsonwu.limited_sale.db.po.LimitedSaleEvent;

import java.util.List;

public interface LimitedSaleEventMapper {
    int deleteByPrimaryKey(Long eventId);

    int insert(LimitedSaleEvent record);

    int insertSelective(LimitedSaleEvent record);

    LimitedSaleEvent selectByPrimaryKey(Long eventId);

    int updateByPrimaryKeySelective(LimitedSaleEvent record);

    int updateByPrimaryKey(LimitedSaleEvent record);

    List<LimitedSaleEvent> queryLimitedSaleEventsByStatus(int eventStatus);

    int deductStock(long eventId);

    int lockStock(long eventId);

    boolean revertStock(long eventId);
}