package com.kgc.dm.service;

import com.kgc.dm.common.Dto;
import com.kgc.dm.vo.CreateOrderVo;

/**
 * 创建订单接口
 */
public interface CreateOrder {


    /**
     * 下单
     */
    public Dto createorder(CreateOrderVo createOrderVo)throws Exception;

}
