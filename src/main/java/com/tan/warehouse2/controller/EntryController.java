package com.tan.warehouse2.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Entry;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.bean.Sku;
import com.tan.warehouse2.service.EntryService;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @date: 2020-04-29 16:22:11
 * @author: Tan.WL
 */
@RestController
@RequestMapping("/entry")
public class EntryController {

    @Autowired
    EntryService entryService;


    @PostMapping("/checkByConditionForm")
    public Object checkByConditionForm(Entry entry, Integer pageNum, Integer pageSize, String limit){

        if(StringUtils.isBlank(entry.getEntryNum()) && StringUtils.isBlank(entry.getEntryDate())
                && entry.getSupplierId() == null && entry.getWarehouseId() == null
                && StringUtils.isBlank(limit)){
            return Msg.failError("提交的数据有误");
        }
        if(pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1){
            return Msg.failError("提交的数据有误");
        }
        return Msg.success(entryService.checkByConditionForm(entry, pageNum, pageSize, limit));
    }

    @PostMapping("/description")
    @RequiresPermissions("entry:remark")
    public Object description(Entry entry){
        System.out.println(entry);
        if(entry.getDescription() == null || entry.getId() == null ||entry.getId() < 1){
            return Msg.failError("您提交的数据有误");
        }
        int i = entryService.description(entry);
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
        PageInfo<Entry> page = entryService.getAllPage(pageNum, pageSize);
        return Msg.success(page);
    }

    @PostMapping("/checkByCondition")
    public Object checkByCondition(Entry entry, Integer pageNum, Integer pageSize){
        System.out.println(entry);

        if(StringUtils.isBlank(entry.getEntryNum()) && StringUtils.isBlank(entry.getEntryDate()) && entry.getSupplierId() == null && entry.getWarehouseId() == null){
            return Msg.failError("提交的数据有误");
        }
        if(pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1){
            return Msg.failError("提交的数据有误");
        }
        return Msg.success(entryService.checkByCondition(entry, pageNum, pageSize));
        
    }

    @PostMapping("/add")
    @RequiresPermissions("entry:add")
    public Object add(String entryStr){
        System.out.println(entryStr);
        Entry e = JSON.parseObject(entryStr, Entry.class);
        System.out.println(e);

        //判断数据是否有限
        List<Sku> list = e.getProductList();
        String entryDate = e.getEntryDate();
        Integer supplierId = e.getSupplierId();
        Integer warehouseId = e.getWarehouseId();
        Integer batch = e.getBatch();

        if(warehouseId == null || warehouseId < 0){
            return Msg.failError("仓库有误");
        }
        if(supplierId == null || supplierId < 0){
            return Msg.failError("供应商有误");
        }
        if(list == null || list.size() == 0){
            return Msg.failError("请先添加商品");
        }
        if(StringUtils.isBlank(entryDate)){
            return Msg.failError("日期不能为空");
        }else{

//            LocalDate date = LocalDate.now(); // get the current date
//            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy/MM/dd");
//            TemporalAccessor parse1 = df.parse(entryDate);

            Date date =  new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Date parse = null;
            try {
                parse = sdf.parse(entryDate);
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

        String entryNum = "";
        //生成入库单号 -- 日期(8位) + warehouseId(3位) + 供应商id（4位）+ 批次(3位)
        List<Integer> dateStr = MyStrUtil.intSplit(entryDate, "/");
        System.out.println(dateStr);

        String yearStr = String.format("%04d", dateStr.get(0));
        String monStr = String.format("%02d", dateStr.get(1));
        String dStr = String.format("%02d", dateStr.get(2));
        String wStr = String.format("%03d", warehouseId);
        String sStr = String.format("%04d", supplierId);
        String batchStr = String.format("%03d", batch);
//        DecimalFormat d4 = new DecimalFormat("0000");
//        DecimalFormat d3 = new DecimalFormat("000");
//        DecimalFormat d2 = new DecimalFormat("00");
//
//        String yearStr = d4.format(dateStr.get(0));
//        String monStr = d2.format(dateStr.get(1));
//        String dStr = d2.format(dateStr.get(2));
//        String wStr = d3.format(warehouseId);
//        String sStr = d4.format(supplierId);
//        String batchStr = d3.format(batch);

        entryNum = yearStr + monStr + dStr + wStr + sStr + batchStr + "";
        System.out.println(entryNum);

        //判断当前批次是否已经存在 --
        //日期(8位) + warehouseId(3位) + 供应商id（4位）+ 批次(3位)
        Entry entry =  entryService.checkBatchByEntryNum(entryNum);
        if(entry != null){
            return Msg.failError("该批次已经存在");
        }
        e.setEntryNum(entryNum);
        int i = entryService.add(e);

        return Msg.success("");
    }

}
