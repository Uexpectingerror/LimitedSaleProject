package com.orsonwu.limited_sale.db.mappers;

import com.orsonwu.limited_sale.db.po.LimitedSaleUser;

public interface LimitedSaleUserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(LimitedSaleUser record);

    int insertSelective(LimitedSaleUser record);

    LimitedSaleUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(LimitedSaleUser record);

    int updateByPrimaryKey(LimitedSaleUser record);
}