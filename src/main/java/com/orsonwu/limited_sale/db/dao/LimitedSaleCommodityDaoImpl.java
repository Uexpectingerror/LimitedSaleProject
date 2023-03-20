package com.orsonwu.limited_sale.db.dao;

import com.orsonwu.limited_sale.db.mappers.LimitedSaleCommodityMapper;
import com.orsonwu.limited_sale.db.po.LimitedSaleCommodity;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class LimitedSaleCommodityDaoImpl implements LimitedSaleCommodityDao {

    @Resource
    private LimitedSaleCommodityMapper limitedSaleCommodityMapper;

    @Override
    public LimitedSaleCommodity queryLimitedSaleCommodityById(long commodityId) {
        return limitedSaleCommodityMapper.selectByPrimaryKey(commodityId);
    }
}
