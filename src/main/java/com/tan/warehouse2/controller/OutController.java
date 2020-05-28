package com.tan.warehouse2.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Notification;
import com.tan.warehouse2.bean.Out;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.bean.Sku;
import com.tan.warehouse2.newspush.MyWebSocket;
import com.tan.warehouse2.service.OutService;
import com.tan.warehouse2.service.SkuService;
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
 * @date: 2020-04-29 16:22:11
 * @author: Tan.WL
 */
@RestController
@RequestMapping("/out")
public class OutController {

    @Autowired
    OutService outService;
    @Autowired
    SkuService skuService;

    @Autowired
    MyWebSocket webSocket;


    @PostMapping("/checkByConditionForm")
    public Object checkByConditionForm(Out out, Integer pageNum, Integer pageSize, String limit){
        System.out.println(out);

        if(StringUtils.isBlank(out.getOutNum()) && StringUtils.isBlank(out.getOutDate())
                && out.getClientId() == null && out.getWarehouseId() == null && StringUtils.isBlank(limit)){
            return Msg.failError("提交的数据有误");
        }
        if(pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1){
            return Msg.failError("提交的数据有误");
        }
        return Msg.success(outService.checkByConditionForm(out, pageNum, pageSize, limit));

    }


    @PostMapping("/getSkus")
    public Object getSkus(Integer outId){
        if(outId == null || outId < 1){
            return Msg.failError("提交的数据有误");
        }
        List<Sku> skus = outService.getSkus(outId);
        return Msg.success(skus);
    }

    @PostMapping("/getOutByDateWarehouse")
    public Object getOutByDateWarehouse(String outDate, Integer warehouseId){

        if(StringUtils.isBlank(outDate) || warehouseId == null || warehouseId < 1){
            return Msg.failError("提交的数据有误！");
        }
        List<Out> outs = outService.getOutByDateWarehouse(outDate, warehouseId);
        return Msg.success(outs);
    }

    @PostMapping("/description")
    @RequiresPermissions("out:remark")
    public Object description(Out out){
        System.out.println(out);
        if(out.getDescription() == null || out.getId() == null ||out.getId() < 1){
            return Msg.failError("您提交的数据有误");
        }
        int i = outService.description(out);
        if(i != 0){
            return Msg.success("");
        }
        return Msg.failError("更新失败");
    }

    @PostMapping("/getAllPage")
    public Object getAllPage(Integer pageNum, Integer pageSize){

        if(pageNum == null || pageSize == null ||pageNum < 0 || pageSize < 1){
            return Msg.failError("您提交的数据有误！");
        }
        PageInfo<Out> page = outService.getAllPage(pageNum, pageSize);
        return Msg.success(page);
    }

    @PostMapping("/checkByCondition")
    public Object checkByCondition(Out out, Integer pageNum, Integer pageSize){
        System.out.println(out);

        if(StringUtils.isBlank(out.getOutNum()) && StringUtils.isBlank(out.getOutDate()) && out.getClientId() == null && out.getWarehouseId() == null){
            return Msg.failError("提交的数据有误");
        }
        if(pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1){
            return Msg.failError("提交的数据有误");
        }
        return Msg.success(outService.checkByCondition(out, pageNum, pageSize));
        
    }

    @PostMapping("/add")
    @RequiresPermissions("out:add")
    public Object add(String outStr){
        Out e = JSON.parseObject(outStr, Out.class);

        //判断数据是否有限
        List<Sku> list = e.getProductList();
        String outDate = e.getOutDate();
        Integer clientId = e.getClientId();
        Integer warehouseId = e.getWarehouseId();
        Integer batch = e.getBatch();

        if(warehouseId == null || warehouseId < 0){
            return Msg.failError("仓库有误");
        }

        if(clientId == null || clientId < 0){
            return Msg.failError("供应商有误");
        }

        int flag =  0;
        List<Sku> alarmSkus = new ArrayList<>();
        if(list == null || list.size() == 0){
            return Msg.failError("请先添加商品");
        }else{//判断数量是否小于库存
            for (Sku sku : list) {
                Sku s = skuService.getSkuById(sku.getId());
                Integer sNum = s.getNum();
                Integer  skuNum = sku.getNum();
                Integer alarmValue = s.getAlarmValue();
                if(s.getNum() < sku.getNum()){
                    flag++;
                }

                //判断出库成功后是否要报警 -- 数量低于警界值
                if((sNum - skuNum) <= alarmValue){
                    s.setNum(sNum - skuNum);
                    alarmSkus.add(s);
                }
            }
        }
        if(flag > 0){
            return Msg.failError("库存数量不足！");
        }

        if(StringUtils.isBlank(outDate)){
            return Msg.failError("日期不能为空");
        }else{

//            LocalDate date = LocalDate.now(); // get the current date
//            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy/MM/dd");
//            TemporalAccessor parse1 = df.parse(outDate);

            Date date =  new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Date parse = null;
            try {
                parse = sdf.parse(outDate);
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
        }
        if(batch == null || batch < 1){
            return Msg.failError("批次有误！");
        }

        String outNum = "";
        //生成入库单号 -- 日期(8位) + warehouseId(3位) + 供应商id（4位）+ 批次(3位)
        List<Integer> dateStr = MyStrUtil.intSplit(outDate, "/");
        System.out.println(dateStr);

        String yearStr = String.format("%04d", dateStr.get(0));
        String monStr = String.format("%02d", dateStr.get(1));
        String dStr = String.format("%02d", dateStr.get(2));
        String wStr = String.format("%03d", warehouseId);
        String sStr = String.format("%04d", clientId);
        String batchStr = String.format("%03d", batch);
//        DecimalFormat d4 = new DecimalFormat("0000");
//        DecimalFormat d3 = new DecimalFormat("000");
//        DecimalFormat d2 = new DecimalFormat("00");
//
//        String yearStr = d4.format(dateStr.get(0));
//        String monStr = d2.format(dateStr.get(1));
//        String dStr = d2.format(dateStr.get(2));
//        String wStr = d3.format(warehouseId);
//        String sStr = d4.format(clientId);
//        String batchStr = d3.format(batch);

        outNum = yearStr + monStr + dStr + wStr + sStr + batchStr + "";
        System.out.println(outNum);

        //判断当前批次是否已经存在 --
        //日期(8位) + warehouseId(3位) + 供应商id（4位）+ 批次(3位)
        Out out =  outService.checkBatchByOutNum(outNum);
        if(out != null){
            return Msg.failError("该批次已经存在");
        }
        e.setOutNum(outNum);
        int i = outService.add(e);
        if(i != 0){
            //查看是否要报警
            if (alarmSkus.size() > 0) {
                for (Sku s : alarmSkus) {
                    //报警操作
                    Notification notification = new Notification();
                    notification.setTitle("库存紧张");
                    notification.setContent(JSON.toJSONString(s));
                    //封装数据
                    MyWebSocket.createNotificatioin(notification);
                }
            }
            return Msg.success("");
        }
        return Msg.failError("更新失败！");

    }

}
