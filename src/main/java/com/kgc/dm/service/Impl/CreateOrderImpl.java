package com.kgc.dm.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.kgc.dm.client.*;
import com.kgc.dm.common.*;
import com.kgc.dm.exception.OrderErrorCode;
import com.kgc.dm.pojo.*;
import com.kgc.dm.service.CreateOrder;
import com.kgc.dm.vo.CreateOrderVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * 订单创建
 */
@Service
public class CreateOrderImpl implements CreateOrder {

    @Autowired
    private RestDmItemClient restDmItemClient;

    @Autowired
    private RestDmSchedulerSeatClient restDmSchedulerSeatClient;

    @Autowired
    private RestDmSchedulerSeatPriceClient schedulerSeatPriceClient;

    @Autowired
    private RestDmOrderClient restDmOrderClient;

    @Autowired
    private RestDmLinkUserClient restDmLinkUserClient;

    @Autowired
    private RestDmOrderLinkUserClient restDmOrderLinkUserClient;

    @Override
    public Dto createorder(CreateOrderVo createOrderVo) throws Exception {
        //先查询对应商品的商品信息，如果没有，则直接返回错误信息

        DmItem dmItemById = restDmItemClient.getDmItemById(createOrderVo.getItemId());
        if (EmptyUtils.isEmpty(dmItemById)) {
            throw new BaseException(OrderErrorCode.ORDER_NO_DATA);
        }
        //生成订单唯一编号
        String orderId = IdWorker.getId();
        //拆分作为集合信息
        String[] split = createOrderVo.getSeatPositions().split(",");
        //总价格
        double totalAmount = 0;
        //座位价格集合
        Double[] areaprice = new Double[split.length];
        for (int i = 0; i < split.length; i++) {
            String[] split1 = split[i].split("_");
            //查询每个座位的信息
            DmSchedulerSeat dmSchedulerSeatByOrder = restDmSchedulerSeatClient.getDmSchedulerSeatByOrder(createOrderVo.getSchedulerId(),
                    Integer.parseInt(split1[0]),
                    Integer.parseInt(split1[1]));
            //更新座位的状态为锁定状态
            dmSchedulerSeatByOrder.setStatus(Constants.SchedulerSeatStatus.SchedulerSeat_TOPAY);
            //更新下单用户
            dmSchedulerSeatByOrder.setUserId(createOrderVo.getUserId());
            dmSchedulerSeatByOrder.setCreatedTime(new Date());
            dmSchedulerSeatByOrder.setUpdatedTime(new Date());
            //更新订单编号
            dmSchedulerSeatByOrder.setOrderNo(orderId);
            //更新数据库
            //restDmSchedulerSeatClient.qdtxModifyDmSchedulerSeat(dmSchedulerSeatByOrder);
            //计算总价格
            DmSchedulerSeatPrice dmSchedulerSeatPrice = schedulerSeatPriceClient.getschedulerseatpriceBySchedulerIdAndArea(dmSchedulerSeatByOrder.getScheduleId(),
                    dmSchedulerSeatByOrder.getAreaLevel());
            //加上用户所加上的所有座位的价格
            areaprice[i] = dmSchedulerSeatPrice.getPrice();
            totalAmount += dmSchedulerSeatPrice.getPrice();
            //保存座位价格信息
        }
        //生成订单信息
        DmOrder dmOrder = new DmOrder();
        dmOrder.setOrderNo(orderId);
        BeanUtils.copyProperties(orderId, dmOrder);
        dmOrder.setItemName(dmItemById.getItemName());
        dmOrder.setOrderType(Constants.SchedulerSeatStatus.SchedulerSeat_TOPAY);
        dmOrder.setTotalCount(areaprice.length);
        //如果勾选了订单保险,那么需要把保险的钱加入到订单总价格里面
        if (createOrderVo.getIsNeedInsurance() == Constants.OrderStatus.ISNEEDINSURANCE_YES) {
            totalAmount += Constants.OrderStatus.NEEDINSURANCE_MONEY;
        }
        dmOrder.setInsuranceAmount(Constants.OrderStatus.NEEDINSURANCE_MONEY);
        dmOrder.setCreatedTime(new Date());
        //更新订单插入订单数据，并且放回当前订单数据的主键ID
        Long order = restDmOrderClient.qdtxAddDmOrder(dmOrder);
        //更新相关联系人
        String[] links = createOrderVo.getLinkIds().split(",");
        for (int i = 0; i < links.length; i++) {
            //查询联系人相关信息
            DmLinkUser dmLinkUser = restDmLinkUserClient.getDmLinkUserById(Long.parseLong(links[i]));
            if (EmptyUtils.isEmpty(dmLinkUser)) {
                throw new BaseException(OrderErrorCode.ORDER_NO_DATA);
            }
            DmOrderLinkUser dmOrderLinkUser = new DmOrderLinkUser();
            dmOrderLinkUser.setOrderId(Long.valueOf(orderId));
            dmOrderLinkUser.setLinkUserId(dmLinkUser.getUserId());
            dmOrderLinkUser.setLinkUserName(dmLinkUser.getName());
            dmOrderLinkUser.setX(Integer.valueOf(split[i].split("_")[0]));
            dmOrderLinkUser.setY(Integer.valueOf(split[i].split("_")[1]));
            dmOrderLinkUser.setPrice(areaprice[i]);
            dmOrderLinkUser.setCreatedTime(new Date());
            restDmOrderLinkUserClient.qdtxAddDmOrderLinkUser(dmOrderLinkUser);


        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("orderId", orderId);
        return DtoUtil.returnDataSuccess(jsonObject);
    }
}
