package com.tan.warehouse2.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.*;
import com.tan.warehouse2.newspush.MyWebSocket;
import com.tan.warehouse2.service.CheckService;
import com.tan.warehouse2.utils.MyStrUtil;
import jodd.util.StringUtil;
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
 * @date: 2020-05-07 17:29:49
 * @author: Tan.WL
 */
@RestController
@RequestMapping("/check")
public class CheckController {

    @Autowired
    CheckService checkService;

    @PostMapping("/checkByConditionForm")
    public Object checkByConditionForm(Check check, Integer pageNum, Integer pageSize, String limit){
        System.out.println(check);

        if(StringUtils.isBlank(check.getCheckSn()) && StringUtils.isBlank(check.getCheckDate())
                && check.getWarehouseId() == null && StringUtils.isBlank(limit)){
            return Msg.failError("提交的数据有误");
        }
        if(pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1){
            return Msg.failError("提交的数据有误");
        }
        return Msg.success(checkService.checkByConditionForm(check, pageNum, pageSize,limit));

    }


    @PostMapping("/description")
    @RequiresPermissions("check:remark")
    public Object description(Integer checkId, Integer skuId, String remark){
        if(StringUtils.isBlank(remark) || checkId == null || checkId < 1 || skuId == null || skuId < 1){
            return Msg.failError("您提交的数据有误");
        }
        checkService.remark(checkId, skuId, remark);

        return Msg.success("");
    }

    @PostMapping("/getAllPage")
    public Object getAllPage(Integer pageNum, Integer pageSize){

        if(pageNum == null || pageSize == null ||pageNum < 0 || pageSize < 1){
            return Msg.failError("您提交的数据有误！");
        }
        PageInfo<Check> page = checkService.getAllPage(pageNum, pageSize);
        return Msg.success(page);
    }

    @PostMapping("/checkByCondition")
    public Object checkByCondition(Check check, Integer pageNum, Integer pageSize){
        System.out.println(check);

        if(StringUtils.isBlank(check.getCheckSn()) && StringUtils.isBlank(check.getCheckDate()) && check.getWarehouseId() == null){
            return Msg.failError("提交的数据有误");
        }
        if(pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1){
            return Msg.failError("提交的数据有误");
        }
        return Msg.success(checkService.checkByCondition(check, pageNum, pageSize));

    }

    @PostMapping("/add")
    @RequiresPermissions("check:add")
    public Object add(String checkStr){
        Check c = JSON.parseObject(checkStr, Check.class);

        //判断数据是否有限
        List<Sku> list = c.getProductList();
        String checkDate = c.getCheckDate();
        Integer warehouseId = c.getBatch();
        String warehouseName = c.getWarehouseName();
        String operator = c.getOperator();
        Integer batch = c.getBatch();

        if(warehouseId == null || warehouseId < 0 || StringUtils.isBlank(warehouseName) || StringUtils.isBlank(checkDate)
                || list == null || list.size() == 0 || StringUtils.isBlank(operator) || batch == null || batch < 1){
            return Msg.failError("提交的数据有误");
        }

        Date date =  new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date parse = null;
        try {
            parse = sdf.parse(checkDate);
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

        if(batch < 1){
            return Msg.failError("批次有误！");
        }



        String checkSn = "";
        //生成入库单号 -- 日期(8位) + warehouseId(3位) + 批次(3位)
        List<Integer> dateStr = MyStrUtil.intSplit(checkDate, "/");
        System.out.println(dateStr);

        String yearStr = String.format("%04d", dateStr.get(0));
        String monStr = String.format("%02d", dateStr.get(1));
        String dStr = String.format("%02d", dateStr.get(2));
        String wStr = String.format("%03d", warehouseId);
        String batchStr = String.format("%03d", batch);

        checkSn = yearStr + monStr + dStr + wStr + batchStr + "";
        System.out.println(checkSn);

        //判断当前批次是否已经存在 --
        //日期(8位) + warehouseId(3位) + 供应商id（4位）+ 批次(3位)
        Check check =  checkService.checkBatchByCheckSn(checkSn);
        if(check != null){
            return Msg.failError("该批次已经存在");
        }
        c.setCheckSn(checkSn);
        checkService.add(c);
        return Msg.success("");

    }
}
