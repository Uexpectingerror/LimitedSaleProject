package com.orsonwu.limited_sale.db.mappers;

import com.orsonwu.limited_sale.db.po.LimitedSaleOrder;

public interface LimitedSaleOrderMapper {
    int deleteByPrimaryKey(Long orderId);

    int insert(LimitedSaleOrder record);

    int insertSelective(LimitedSaleOrder record);

    LimitedSaleOrder selectByPrimaryKey(Long orderId);

    int updateByPrimaryKeySelective(LimitedSaleOrder record);

    int updateByPrimaryKey(LimitedSaleOrder record);

    LimitedSaleOrder selectByOrderNum(String orderNum);
}