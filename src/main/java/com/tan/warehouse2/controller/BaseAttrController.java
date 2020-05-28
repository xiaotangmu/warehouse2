package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.BaseAttr;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.service.BaseAttrService;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/baseAttr")
public class BaseAttrController {

    @Autowired
    BaseAttrService baseAttrService;

    @PostMapping("/delete")
    @RequiresPermissions("attr:delete")
    public Object delete(String names, String ids, Integer catalog3Id){
        if(StringUtils.isNotBlank(names) && StringUtils.isNotBlank(ids) && catalog3Id != null && catalog3Id > 0){
            List<Integer> integers = MyStrUtil.intSplit(ids, "-");
            List<String> strings = MyStrUtil.strSplit(names, "-");
            baseAttrService.delete(strings, integers, catalog3Id);
            return Msg.success("删除成功！");
        }else{
            Msg.failError("提交的数据有问题！");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("attr:update")
    public Object update(BaseAttr baseAttr, String oldName){
        System.out.println("update ..." + baseAttr);
        if (StringUtils.isNotBlank(baseAttr.getName()) && baseAttr.getId() != null && baseAttr.getId() > 0 && StringUtils.isNotBlank(oldName)){
            int update = baseAttrService.update(baseAttr, oldName);
            if (update != 0) {
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }

//    @PostMapping("/find")
////    @GetMapping("/findBaseAttr")
//    public Object findBaseAttr(String name, Integer pageNum, Integer pageSize){
//        return Msg.success(baseAttrService.getBaseAttrLikeName(name, pageNum, pageSize));
//    }

//    @GetMapping("/getAllPage")
//    public Object getAllPage(Integer pageNum, Integer pageSize){
//        return Msg.success(baseAttrService.getAllPage(pageNum, pageSize));
//    }

    @GetMapping("/getAll")
    @RequiresPermissions("attr:get")
    public Object getAll(Integer catalog3Id){
        if(catalog3Id == null || catalog3Id <= 0){
            return Msg.failError("提交的参数有误！");
        }
        return Msg.success(baseAttrService.getAll(catalog3Id));
    }

    @PostMapping("/add")
    @RequiresPermissions("attr:add")
    public Object add(BaseAttr baseAttr){//添加品牌

        System.out.println(baseAttr);

        String name = baseAttr.getName();
        Integer catalog3Id = baseAttr.getCatalog3Id();
        //判断name 是否为空
        if(StringUtils.isBlank(baseAttr.getName())){
            return Msg.noCondition("name不能为空");
        }
        if(catalog3Id == null || catalog3Id <= 0){
            return Msg.failError("提交的数据有误！");
        }
        //判断名字是否重复

        BaseAttr baseAttr2 = baseAttrService.getBaseAttrByName(name,catalog3Id);
        if (baseAttr2 != null){
            return Msg.noCondition("该name已经存在");
        }
        int add = baseAttrService.add(baseAttr);
        if (add != 0 && add != -1) {
            if (baseAttr.getId() != null) {
                return Msg.success(baseAttr);
            }
        }else if (add == -1){
            return Msg.noCondition("该name已经存在");
        }
        return Msg.failError("添加失败！");
    }

    @PostMapping("/nameCheck")
//    @GetMapping("/nameCheck")
    public Object nameCheck(String name, Integer catalog3Id){
        System.out.println(name + catalog3Id);
        if(catalog3Id == null || catalog3Id <= 0){
            return Msg.failError("提交的参数有误！");
        }
        if (StringUtils.isBlank(name)) {
            return Msg.failError("名字不能为空！");
        }
        BaseAttr baseAttr = baseAttrService.getBaseAttrByName(name, catalog3Id);
        if(baseAttr == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }


    @PostMapping("/getAttrAndValue")
    public Object getAttrAndValue(Integer spuId){
        if(spuId == null || spuId < 1){
            return Msg.failError("您提交的数据有误！");
        }
        return Msg.success(baseAttrService.getAttrAndValueBySpuId(spuId));
    }

}
