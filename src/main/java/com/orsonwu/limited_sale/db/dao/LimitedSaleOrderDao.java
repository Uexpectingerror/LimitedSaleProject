package com.orsonwu.limited_sale.db.dao;

import com.orsonwu.limited_sale.db.po.LimitedSaleOrder;

public interface LimitedSaleOrderDao {
    void insertOrder(LimitedSaleOrder order);

    LimitedSaleOrder queryOrder(String orderNo);

    void updateOrder(LimitedSaleOrder order);
}
