package com.orsonwu.limited_sale.db.mappers;

import com.orsonwu.limited_sale.db.po.LimitedSaleCommodity;

public interface LimitedSaleCommodityMapper {
    int deleteByPrimaryKey(Long commodityId);

    int insert(LimitedSaleCommodity record);

    int insertSelective(LimitedSaleCommodity record);

    LimitedSaleCommodity selectByPrimaryKey(Long commodityId);

    int updateByPrimaryKeySelective(LimitedSaleCommodity record);

    int updateByPrimaryKey(LimitedSaleCommodity record);
}