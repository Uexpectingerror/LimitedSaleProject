package com.orsonwu.limited_sale.db.dao;

import com.orsonwu.limited_sale.db.po.LimitedSaleCommodity;

public interface LimitedSaleCommodityDao {

    public LimitedSaleCommodity queryLimitedSaleCommodityById(long commodityId);
}
