package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Catalog2;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.service.Catalog2Service;
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
@RequestMapping("/catalog2")
public class Catalog2Controller {

    @Autowired
    Catalog2Service catalog2Service;

    @PostMapping("/delete")
    @RequiresPermissions("catalog:delete")
    public Object delete(String names, String ids, String ztreeIds, Integer catalog1Id){
        if(StringUtils.isNotBlank(names) && StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(ztreeIds) && catalog1Id != null && catalog1Id > 0){
            List<Integer> integers = MyStrUtil.intSplit(ids, "-");
            List<String> strings = MyStrUtil.strSplit(names, "-");
            List<String> strings2 = MyStrUtil.strSplit(ztreeIds, "-");
            catalog2Service.delete(strings, integers,strings2, catalog1Id);
            return Msg.success("删除成功！");
        }else{
            Msg.failError("提交的数据有问题！");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("catalog:update")
    public Object update(Catalog2 catalog2,String oldName){
//        System.out.println("update ..." + catalog2);
//        System.out.println("update ..." + oldName);
        if (StringUtils.isNotBlank(catalog2.getName()) && catalog2.getId() != null && catalog2.getId() > 0 && StringUtils.isNotBlank(oldName)){
            int update = catalog2Service.update(catalog2,oldName);
            if (update != 0) {
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }

//    @PostMapping("/find")
////    @GetMapping("/findCatalog2")
//    public Object findCatalog2(String name, Integer pageNum, Integer pageSize){
//        return Msg.success(catalog2Service.getCatalog2LikeName(name, pageNum, pageSize));
//    }

//    @GetMapping("/getAllPage")
//    public Object getAllPage(Integer pageNum, Integer pageSize){
//        return Msg.success(catalog2Service.getAllPage(pageNum, pageSize));
//    }
    @GetMapping("/getAll")
    public Object getAll(Integer catalog1Id){
        if (catalog1Id == null || catalog1Id < 1) {
            return Msg.failError("catalog1Id 为空");
        }
        return Msg.success(catalog2Service.getAll(catalog1Id));
    }

    @GetMapping("/getAll2")
    public Object getAll2(Integer catalog1Id){
        if (catalog1Id == null || catalog1Id < 1) {
            return Msg.failError("catalog1Id 为空");
        }
        return Msg.success(catalog2Service.getAll2(catalog1Id));
    }

    @PostMapping("/add")
    @RequiresPermissions("catalog:add")
    public Object add(Catalog2 catalog2){//添加品牌

        System.out.println(catalog2);

        //判断name 是否为空
        if(StringUtils.isBlank(catalog2.getName())){
            return Msg.noCondition("品牌名不能为空");
        }
        if(catalog2.getCatalog1Id() == null || catalog2.getCatalog1Id() < 1){
            return Msg.failError("一级分类 id 不能为空");
        }
        //判断名字是否重复
        String name = catalog2.getName();
        Catalog2 catalog22 = catalog2Service.getCatalog2ByName(name, catalog2.getCatalog1Id());
        if (catalog22 != null){
            return Msg.noCondition("该分类已经存在");
        }
        int add = catalog2Service.add(catalog2);
//        System.out.println(catalog2);
        if (add != 0 && add != -1) {
            if (catalog2.getId() != null) {
                return Msg.success(catalog2);
            }
        }else if (add == -1){
            return Msg.noCondition("该品牌已经存在");
        }
        return Msg.failError("添加失败！");
    }

    @PostMapping("/nameCheck")
//    @GetMapping("/nameCheck")
    public Object nameCheck(String name, Integer level1Id){
        System.out.println(name + " level1Id: " + level1Id);
        if (StringUtils.isBlank(name)) {
            return Msg.failError("name不能为空！");
        }
        Catalog2 catalog2 = catalog2Service.getCatalog2ByName(name, level1Id);
        if(catalog2 == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

}
