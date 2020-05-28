package com.tan.warehouse2.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.BaseAttr;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.bean.Sku;
import com.tan.warehouse2.bean.SkuEdit;
import com.tan.warehouse2.service.SkuEditService;
import com.tan.warehouse2.service.SkuService;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/sku")
public class SkuController {

    @Autowired
    SkuService skuService;
    @Autowired
    SkuEditService skuEditService;

    @PostMapping("/updateEditRemark")
    @RequiresPermissions("sku:edit:remark")
    public Object updateEditRemark(SkuEdit skuEdit){

        if (skuEdit.getId() == null || skuEdit.getId() < 1 || skuEdit.getRemark() == null){
            return Msg.failError("提交的数据有误");
        }
        skuEditService.updateEditRemark(skuEdit);
        return Msg.success("");
    }

    @PostMapping("/getSkuEdit")
    public Object getSkuEdit(String limit, Integer warehouseId, String skuEditDate, Integer pageNum, Integer pageSize){
        if(pageNum == null || pageNum < 1 ||pageSize == null || pageSize < 1){
            return Msg.failError("提交的数据有误");
        }
        if(StringUtils.isBlank(limit) && StringUtils.isBlank(skuEditDate)){
            if (warehouseId == null || warehouseId < 1){
                return Msg.failError("提交的数据有误");
            }
        }

        PageInfo<SkuEdit> pageInfo = skuEditService.getSkuEditByCondition(limit, warehouseId, skuEditDate, pageNum, pageSize);
        return Msg.success(pageInfo);

    }

    @PostMapping("/getAllEdit")
    public Object getAllEdit(Integer pageNum, Integer pageSize){
        if(pageNum == null || pageNum < 1 ||pageSize == null || pageSize < 1){
            return Msg.failError("提交的数据有误");
        }
        PageInfo<SkuEdit> pageInfo = skuEditService.getAllEdit(pageNum, pageSize);
        return Msg.success(pageInfo);
    }

    @PostMapping("/getAllByWarehouseId")
    public Object getAllByWarehouseId(Integer warehouseId){
        if (warehouseId == null || warehouseId < 1) {
            return Msg.failError("提交的数据有误");
        }

        List<Sku> skus = skuService.getAllByWarehouseId(warehouseId);
        return Msg.success(skus);
    }

    @PostMapping("/delete")
    @RequiresPermissions("sku:delete")
    public Object delete(String idStr, String skuEditStr){
        if(StringUtils.isNotBlank(idStr) && StringUtils.isNotBlank(skuEditStr)){
            List<Integer> ids = MyStrUtil.intSplit(idStr, "-");
            int delete = skuService.delete(ids);
            if(delete != 0){
                List<String> skuEdits = MyStrUtil.strSplit(skuEditStr, "---");
                System.out.println(skuEdits);
                for (String skuEdit : skuEdits) {
                    System.out.println(skuEdit);
                    SkuEdit skuEdit1 = JSON.parseObject(skuEdit, SkuEdit.class);
                    System.out.println(skuEdit1);
                    skuEdit1.setSkuId(skuEdit1.getId());
                    skuEdit1.setId(null);
                    skuEditService.insert(skuEdit1);
                }
                return Msg.success("删除成功");
            }
        }else{
            Msg.failError("提交的数据有问题！");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("sku:update")
    public Object update(Sku sku, String skuEdit){
        System.out.println("update ..." + sku);
        Integer id = sku.getId();
        if(id == null || id < 1 || StringUtils.isBlank(skuEdit)){
            return Msg.failError("提交的数据有误");
        }
        int update = skuService.update(sku);
        if(update != 0){
            SkuEdit skuEdit1 = JSON.parseObject(skuEdit, SkuEdit.class);
            skuEditService.insert(skuEdit1);
            return Msg.success("");
        }

        return Msg.failError("更新失败！");
    }

    @PostMapping("/find")
//    @GetMapping("/findSku")
    public Object findSku(String name, Integer pageNum, Integer pageSize){
        return Msg.success(skuService.getSkuLikeName(name, pageNum, pageSize));
    }

    @PostMapping("/getAllByCBWId")
    public Object getAllByCBWId(Integer brandId, Integer catalog3Id, Integer warehouseId){
        if(brandId == null || brandId < 1){
            return Msg.failError("请先选择品牌");
        }
        if(catalog3Id == null || catalog3Id < 1){
            return Msg.failError("请先选择分类");
        }
        if(warehouseId == null || warehouseId < 1){
            return Msg.failError("请先选择仓库");
        }

        List<Sku> skus = skuService.getSkuByCBWId(brandId, catalog3Id, warehouseId);

        return Msg.success(skus);
    }

    @GetMapping("/getAllPage")
    @RequiresPermissions("sku:getAllPage")
    public Object getAllPage(Integer pageNum, Integer pageSize){
        return Msg.success(skuService.getAllPage(pageNum, pageSize));
    }

    @GetMapping("/getAll")
    public Object getAll(Integer catalog3Id, Integer brandId){
        if(catalog3Id == null || catalog3Id <= 0 || brandId == null || brandId <= 0){
            return Msg.failError("提交的参数有误！");
        }
        return Msg.success(skuService.getAll(catalog3Id,brandId));
    }

    @PostMapping("/add")
    @RequiresPermissions("sku:add")
    public Object add(String skuStr){//添加品牌

        System.out.println(skuStr);
        Sku sku = JSON.parseObject(skuStr, Sku.class);
        System.out.println(sku);

        Integer spuId = sku.getSpuId();
        Integer warehouseId = sku.getWarehouseId();
        String unit = sku.getUnit();
        Integer catalog3Id = sku.getCatalog3Id();
        Integer brandId = sku.getBrandId();
        if(spuId == null || spuId < 1){
            return Msg.failError("请先添加商品");
        }
        if(catalog3Id == null || catalog3Id < 1){
            return Msg.failError("请先选择分类");
        }
        if(brandId == null || brandId < 1){
            return Msg.failError("请先选择品牌");
        }
        if(warehouseId == null || warehouseId < 1){
            return Msg.failError("请先选择仓库");
        }
        if(StringUtils.isBlank(unit)){
            return Msg.failError("请先填写商品单位");
        }

        if(sku.getAlarmValue() == null || sku.getAlarmValue() < 0){
            sku.setAlarmValue(0);
        }
        if(StringUtils.isBlank(sku.getDescription())){
            sku.setDescription("");
        }
        //判断该仓库是否已经存在该商品
        //利用 仓库 - warehouseId 商品 - spuId 属性规格 -- attrId valueStr 和 单位 - unit 查重
        List<Sku> skus = skuService.checkSkuByAll(sku);
        System.out.println(skus);
        if (skus != null) {
            return Msg.failError("该规格商品仓库中已存在");
        }

        int add = skuService.add(sku);
        if(add != 0){
            return Msg.success(sku);
        }
        return Msg.failError("添加失败！");
    }

    @PostMapping("/nameCheck")
//    @GetMapping("/nameCheck")
    public Object nameCheck(String name, Integer catalog3Id, Integer brandId){
        System.out.println(name + "-" + catalog3Id + "-" + brandId);
        if(catalog3Id == null || catalog3Id <= 0 || brandId == null || brandId <= 0){
            return Msg.failError("提交的参数有误！");
        }
        if (StringUtils.isBlank(name)) {
            return Msg.failError("名字不能为空！");
        }
        Sku Sku = skuService.getSkuByName(name, catalog3Id, brandId);
        if(Sku == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

}
