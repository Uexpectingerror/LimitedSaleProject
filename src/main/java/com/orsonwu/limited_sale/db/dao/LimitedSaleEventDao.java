package com.orsonwu.limited_sale.db.dao;

import com.orsonwu.limited_sale.db.po.LimitedSaleEvent;

import java.util.List;

public interface LimitedSaleEventDao {

    public List<LimitedSaleEvent> queryLimitedSaleEventsByStatus(int eventStatus);

    public void insertLimitedSaleEvent(LimitedSaleEvent saleEvent);

    public LimitedSaleEvent queryLimitedSaleEventById(long eventId);

    public void updateLimitedSaleEvent(LimitedSaleEvent saleEvent);

    public boolean deductStock(long eventId);

    public boolean lockStock(long eventId);

    //add the stock back to the event
    public boolean revertStock(long eventId);
}
