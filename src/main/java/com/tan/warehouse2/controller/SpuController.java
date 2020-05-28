package com.tan.warehouse2.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tan.warehouse2.bean.BaseAttr;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.bean.Spu;
import com.tan.warehouse2.service.SpuService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/spu")
public class SpuController {

    @Autowired
    SpuService spuService;

    @PostMapping("/delete")
    @RequiresPermissions("spu:delete")
    public Object delete(String items){
        if(StringUtils.isNotBlank(items)){
            List<Spu> spus = new ArrayList<>();
            spus = JSON.parseObject(items, new TypeReference<List<Spu>>(){});
            if(spus != null || spus.size() > 0){
//            List<Integer> integers = MyStrUtil.intSplit(ids, "-");
//            List<String> strings = MyStrUtil.strSplit(names, "-");
            spuService.delete(spus);
                return Msg.success("删除成功！");
            }else{
                Msg.failError("提交的数据有问题！");
            }
        }else{
            Msg.failError("提交的数据有问题！");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("spu:update")
    public Object update(Spu spu, String oldName, String attrs, Integer oldBrandId){
        System.out.println("update ..." + spu);
        System.out.println("update ..." + oldName);
        System.out.println("update ..." + attrs);
        System.out.println("update ..." + oldBrandId);
        if(spu == null || oldName == null || oldBrandId == null){
            return Msg.failError("提交的数据有误，更新失败！");
        }
        if (StringUtils.isNotBlank(spu.getName()) && spu.getId() != null && spu.getId() > 0 && StringUtils.isNotBlank(oldName)){
            List<BaseAttr> baseAttrs = JSON.parseObject(attrs, new TypeReference<List<BaseAttr>>() {});
            spu.setBaseAttrs(baseAttrs);
            int update = spuService.update(spu, oldName, oldBrandId);
            if (update != 0) {
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }

    @PostMapping("/find")
//    @GetMapping("/findSpu")
    public Object findSpu(String name, Integer pageNum, Integer pageSize){
        return Msg.success(spuService.getSpuLikeName(name, pageNum, pageSize));
    }

    @GetMapping("/getAllPage")
    public Object getAllPage(Integer pageNum, Integer pageSize){
        return Msg.success(spuService.getAllPage(pageNum, pageSize));
    }

    @GetMapping("/getAll")
    public Object getAll(Integer catalog3Id, Integer brandId){
        if(catalog3Id == null || catalog3Id <= 0 || brandId == null || brandId <= 0){
            return Msg.failError("提交的参数有误！");
        }
        return Msg.success(spuService.getAll(catalog3Id,brandId));
    }

    @PostMapping("/add")
    @RequiresPermissions("spu:add")
    public Object add(Spu spu, String attrs2){//添加品牌

        System.out.println(spu);
        System.out.println(attrs2);
//        List<BaseAttr> baseAttrs = JSON.parseObject(attrs, new TypeReference<List<BaseAttr>>() {});
//        spu.setBaseAttrs(baseAttrs);

        List<BaseAttr> baseAttrs2 = JSON.parseObject(attrs2, new TypeReference<List<BaseAttr>>() {});
        System.out.println(baseAttrs2);
        spu.setBaseAttrs(baseAttrs2);

        String name = spu.getName();
        Integer catalog3Id = spu.getCatalog3Id();
        Integer brandId = spu.getBrandId();
        //判断name 是否为空
        if(StringUtils.isBlank(spu.getName())){
            return Msg.noCondition("name不能为空");
        }
        if(catalog3Id == null || catalog3Id <= 0){
            return Msg.failError("提交的数据有误！");
        }
        //判断名字是否重复

        Spu spu2 = spuService.getSpuByName(name,catalog3Id,brandId);
        if (spu2 != null){
            return Msg.noCondition("该name已经存在");
        }
        int add = spuService.add(spu);
        if (add != 0 && add != -1) {
            if (spu.getId() != null) {
                return Msg.success(spu);
            }
        }else if (add == -1){
            return Msg.noCondition("该name已经存在");
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
        Spu spu = spuService.getSpuByName(name, catalog3Id, brandId);
        if(spu == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

}
