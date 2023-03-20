package com.orsonwu.limited_sale.db.dao;

import com.orsonwu.limited_sale.db.mappers.LimitedSaleOrderMapper;
import com.orsonwu.limited_sale.db.po.LimitedSaleOrder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class LimitedSaleOrderDaoImpl implements LimitedSaleOrderDao{
    @Resource
    private LimitedSaleOrderMapper orderMapper;

    @Override
    public void insertOrder(LimitedSaleOrder order) {
        orderMapper.insert(order);
    }
    @Override
    public LimitedSaleOrder queryOrder(String orderNum) {
        return orderMapper.selectByOrderNum(orderNum);
    }

    @Override
    public void updateOrder(LimitedSaleOrder order)
    {
        orderMapper.updateByPrimaryKey(order);
    }
}
