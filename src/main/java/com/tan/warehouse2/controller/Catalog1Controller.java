package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Catalog1;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.service.Catalog1Service;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalog1")
public class Catalog1Controller {

    @Autowired
    Catalog1Service catalog1Service;

    @PostMapping("/delete")
    @RequiresPermissions("catalog:delete")
    public Object delete(Catalog1 catalog1){
        if(StringUtils.isNotBlank(catalog1.getName()) && catalog1.getId() != null && catalog1.getId() > 0){
            catalog1Service.delete(catalog1);
            return Msg.success("删除成功！");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("catalog:update")
    public Object update(Catalog1 catalog1, String oldName){
        System.out.println("update ..." + catalog1);
        if (StringUtils.isNotBlank(catalog1.getName()) && catalog1.getId() != null && catalog1.getId() > 0 && StringUtils.isNotBlank(oldName)){
            int update = catalog1Service.update(catalog1, oldName);
            if (update != 0) {
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }

//    @PostMapping("/find")
////    @GetMapping("/findCatalog1")
//    public Object findCatalog1(String name, Integer pageNum, Integer pageSize){
//        return Msg.success(catalog1Service.getCatalog1LikeName(name, pageNum, pageSize));
//    }

//    @GetMapping("/getAllPage")
//    public Object getAllPage(Integer pageNum, Integer pageSize){
//        return Msg.success(catalog1Service.getAllPage(pageNum, pageSize));
//    }

    @GetMapping("/getAll")
    public Object getAll(){
        return Msg.success(catalog1Service.getAll());
    }

    @PostMapping("/add")
    @RequiresPermissions("catalog:add")
    public Object add(Catalog1 catalog1){//添加品牌

        System.out.println(catalog1);

        //判断name 是否为空
        if(StringUtils.isBlank(catalog1.getName())){
            return Msg.noCondition("name不能为空");
        }
        //判断名字是否重复
        String name = catalog1.getName();
        Catalog1 catalog12 = catalog1Service.getCatalog1ByName(name);
        if (catalog12 != null){
            return Msg.noCondition("该name已经存在");
        }
        int add = catalog1Service.add(catalog1);
        if (add != 0 && add != -1) {
            if (catalog1.getId() != null) {
                return Msg.success(catalog1);
            }
        }else if (add == -1){
            return Msg.noCondition("该name已经存在");
        }
        return Msg.failError("添加失败！");
    }

    @PostMapping("/nameCheck")
//    @GetMapping("/nameCheck")
    public Object nameCheck(String name){
        System.out.println(name);
        if (StringUtils.isBlank(name)) {
            return Msg.failError("分类名不能为空！");
        }
        Catalog1 catalog1 = catalog1Service.getCatalog1ByName(name);
        if(catalog1 == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

}
