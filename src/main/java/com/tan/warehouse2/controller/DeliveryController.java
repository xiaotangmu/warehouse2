package com.tan.warehouse2.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.*;
import com.tan.warehouse2.newspush.MyWebSocket;
import com.tan.warehouse2.service.DeliveryService;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @date: 2020-05-06 10:33:57
 * @author: Tan.WL
 */
@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    @Autowired
    DeliveryService deliveryService;

    @PostMapping("/checkByConditionForm")
    public Object checkByConditionForm(Delivery delivery, Integer pageNum, Integer pageSize, String limit){
        System.out.println(delivery);

        if(StringUtils.isBlank(delivery.getDeliveryNum()) && StringUtils.isBlank(delivery.getDeliveryDate())
                && delivery.getClientId() == null && StringUtils.isBlank(limit)){
            return Msg.failError("提交的数据有误");
        }
        if(pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1){
            return Msg.failError("提交的数据有误");
        }
        return Msg.success(deliveryService.checkByConditionForm(delivery, pageNum, pageSize, limit));

    }

    @PostMapping("/add")
    @RequiresPermissions("delivery:add")
    public Object add(Delivery delivery){
        System.out.println(delivery);

        if (delivery == null) {
            return Msg.failError("提交的数据有误！");
        }
        //判断数据是否有限
        String address = delivery.getAddress();
        String city = delivery.getCity();
        String deliveryDate = delivery.getDeliveryDate();
        String operator = delivery.getOperator();
        String outNum1 = delivery.getOutNum();
        Integer outId = delivery.getOutId();
        String contact = delivery.getContact();
        String phone = delivery.getPhone();
        Integer batch = delivery.getBatch();
        Integer clientId = delivery.getClientId();

        if (StringUtils.isBlank(address) || StringUtils.isBlank(city) ||
                StringUtils.isBlank(deliveryDate) || StringUtils.isBlank(operator) || StringUtils.isBlank(outNum1)
                || StringUtils.isBlank(contact) || StringUtils.isBlank(phone) || outId == null || outId < 1 || clientId == null || clientId < 1) {
            return Msg.failError("提交的数据有误");
        }

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date parse = null;
        try {
            parse = sdf.parse(deliveryDate);
        } catch (ParseException e1) {
            return Msg.failError("请输入正确日期格式");
        }
        if(parse == null){
            return Msg.failError("请输入正确日期格式");
        }else{
            //日期是否未来
            boolean after = parse.after(date);
            if(after){
                return Msg.failError("请输入有效日期！");
            }
        }

        if(batch == null || batch < 1){
            return Msg.failError("批次有误！");
        }

        String deliveryNum = "";
        //生成入库单号 -- 日期(8位) + 客户（4位）+ 批次(3位)
        List<Integer> dateStr = MyStrUtil.intSplit(deliveryDate, "/");
        System.out.println(dateStr);

        String yearStr = String.format("%04d", dateStr.get(0));
        String monStr = String.format("%02d", dateStr.get(1));
        String dStr = String.format("%02d", dateStr.get(2));
        String sStr = String.format("%04d", clientId);
        String batchStr = String.format("%03d", batch);

        deliveryNum = yearStr + monStr + dStr + sStr + batchStr + "";
        System.out.println(deliveryNum);

        //判断当前批次是否已经存在 --
        //日期(8位) + warehouseId(3位) + 供应商id（4位）+ 批次(3位)
        Delivery d =  deliveryService.checkBatchByDeliveryNum(deliveryNum);
        if(d != null){
            return Msg.failError("该批次已经存在");
        }
        delivery.setDeliveryNum(deliveryNum);
        int i = deliveryService.add(delivery);
        if(i != 0){
            return Msg.success("");
        }
        return Msg.failError("更新失败！");



    }

    @PostMapping("/description")
    @RequiresPermissions("delivery:remark")
    public Object description(Delivery delivery){
        System.out.println(delivery);
        if(delivery.getDescription() == null || delivery.getId() == null ||delivery.getId() < 1){
            return Msg.failError("您提交的数据有误");
        }
        deliveryService.description(delivery);
        return Msg.success("");
    }

    @PostMapping("/getAllPage")
    public Object getAllPage(Integer pageNum, Integer pageSize){

        if(pageNum == null || pageSize == null ||pageNum < 0 || pageSize < 1){
            return Msg.failError("您提交的数据有误！");
        }
        PageInfo<Delivery> page = deliveryService.getAllPage(pageNum, pageSize);
        return Msg.success(page);
    }

    @PostMapping("/checkByCondition")
    public Object checkByCondition(Delivery delivery, Integer pageNum, Integer pageSize){
        System.out.println(delivery);

        if(StringUtils.isBlank(delivery.getDeliveryNum()) && StringUtils.isBlank(delivery.getDeliveryDate()) && delivery.getClientId() == null ){
            return Msg.failError("提交的数据有误");
        }
        if(pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1){
            return Msg.failError("提交的数据有误");
        }
        return Msg.success(deliveryService.checkByCondition(delivery, pageNum, pageSize));

    }

}
