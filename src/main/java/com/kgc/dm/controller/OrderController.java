package com.kgc.dm.controller;

import com.kgc.dm.common.Dto;
import com.kgc.dm.service.CreateOrder;
import com.kgc.dm.vo.CreateOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/v", method = RequestMethod.POST)
public class OrderController {

    @Autowired
    CreateOrder createOrder;

    @ResponseBody
    @RequestMapping(value = "/createorder", method = RequestMethod.POST)
    public Dto creatorder(@RequestBody CreateOrderVo createOrderVo)throws Exception{
        return createOrder.createorder(createOrderVo);
    }

}
