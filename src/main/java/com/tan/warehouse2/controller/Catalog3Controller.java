package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Catalog3;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.service.Catalog3Service;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalog3")
public class Catalog3Controller {

    @Autowired
    Catalog3Service catalog3Service;

    @PostMapping("/delete")
    @RequiresPermissions("catalog:delete")
    public Object delete(String names, String ids, String ztreeIds, Integer catalog1Id, Integer catalog2Id){
        if(StringUtils.isNotBlank(names) && StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(ztreeIds)
                && catalog1Id != null && catalog1Id > 0 && catalog2Id != null && catalog2Id > 0){
            List<Integer> integers = MyStrUtil.intSplit(ids, "-");
            List<String> strings = MyStrUtil.strSplit(names, "-");
            List<String> strings2 = MyStrUtil.strSplit(ztreeIds, "-");
            catalog3Service.delete(strings, integers, strings2, catalog1Id, catalog2Id);
            return Msg.success("删除成功！");
        }else{
            return Msg.failError("请求数据有误！");
        }
    }

    @PostMapping("/update")
    @RequiresPermissions("catalog:update")
    public Object update(Catalog3 catalog3, String oldName){
        System.out.println("update ..." + catalog3);
        if (StringUtils.isNotBlank(catalog3.getName()) && catalog3.getId() != null && catalog3.getId() > 0 && StringUtils.isNotBlank(oldName)){
            int update = catalog3Service.update(catalog3, oldName);
            if (update != 0) {
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }

//    @PostMapping("/find")
////    @GetMapping("/findCatalog3")
//    public Object findCatalog3(String name, Integer pageNum, Integer pageSize){
//        return Msg.success(catalog3Service.getCatalog3LikeName(name, pageNum, pageSize));
//    }

    @GetMapping("/getAll")
    public Object getAll(Integer catalog2Id){
        if (catalog2Id == null || catalog2Id < 1) {
            return Msg.failError("catalog1Id 为空");
        }
        return Msg.success(catalog3Service.getAll(catalog2Id));
    }
    @GetMapping("/getAll2")
    public Object getAll2(Integer catalog2Id, Integer catalog1Id){
        if (catalog2Id == null || catalog2Id < 1) {
            return Msg.failError("catalog1Id 为空");
        }
        return Msg.success(catalog3Service.getAll2(catalog2Id,catalog1Id));
    }

//    @GetMapping("/getAllPage")
//    public Object getAllPage(Integer pageNum, Integer pageSize){
//        return Msg.success(catalog3Service.getAllPage(pageNum, pageSize));
//    }

    @PostMapping("/add")
    @RequiresPermissions("catalog:add")
    public Object add(Catalog3 catalog3){//添加品牌

        System.out.println(catalog3);

        //判断name 是否为空
        if(StringUtils.isBlank(catalog3.getName())){
            return Msg.noCondition("品牌名不能为空");
        }
        if(catalog3.getCatalog1Id() == null || catalog3.getCatalog1Id() < 1){
            return Msg.failError("一级分类 id 不能为空");
        }
        if(catalog3.getCatalog2Id() == null || catalog3.getCatalog2Id() < 1){
            return Msg.failError("二级分类 id 不能为空");
        }
        //判断名字是否重复
        String name = catalog3.getName();
        Catalog3 catalog32 = catalog3Service.getCatalog3ByName(name,catalog3.getCatalog2Id());
        if (catalog32 != null){
            return Msg.noCondition("该品牌已经存在");
        }
        int add = catalog3Service.add(catalog3);
        if (add != 0 && add != -1) {
            if (catalog3.getId() != null) {
                return Msg.success(catalog3);
            }
        }else if (add == -1){
            return Msg.noCondition("该分类已经存在");
        }
        return Msg.failError("添加失败！");
    }

    @PostMapping("/nameCheck")
//    @GetMapping("/nameCheck")
    public Object nameCheck(String name, Integer catalog2Id){
        System.out.println(name);
        if (StringUtils.isBlank(name)) {
            return Msg.failError("分类名不能为空！");
        }
        Catalog3 catalog3 = catalog3Service.getCatalog3ByName(name, catalog2Id);
        if(catalog3 == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

}
