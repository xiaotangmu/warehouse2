package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Authority;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.service.AuthorityService;
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

@RestController
@RequestMapping("/authority")
public class AuthorityController {

    @Autowired
    AuthorityService authorityService;

    @PostMapping("/delete")
    @RequiresPermissions("authority:delete")
    public Object delete(String ids){
        List<Integer> list = MyStrUtil.intSplit(ids,"-");
        if(list == null || list.size() <= 0){
            return Msg.failError("您提交的数据有误！");
        }
        int result = authorityService.delete(list);
        if (result != 0) {
            return Msg.success("删除成功");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("authority:update")
    public Object update(Authority authority, String oldName){

        if (authority.getId() == null || authority.getId() == null || authority.getId() < 1){
            return Msg.failError("您提交的数据有误");
        }
        Integer level = authority.getLevel();
        Integer pId = authority.getPId();
        if(level == null || level < 0){
            return Msg.failError("您提交的数据有误");
        }else if(level > 0){
            if (pId == null || pId < 1) {
                return Msg.failError("您提交的数据有误");
            }
        }

        int result = authorityService.update(authority, oldName);
        if (result == 0){
            return Msg.failError("更新失败！");
        }else if(result == -1){
            return Msg.noCondition("name 已经存在");
        }
        return Msg.success(1);
    }

    @PostMapping("/nameCheck")
    public Object nameCheck(Authority authority){
        String name = authority.getAuthName();
        Integer level = authority.getLevel();
        Integer pId = authority.getPId();
        if(StringUtils.isBlank(name) || level == null || level > 2 || level < 0 || pId == null || pId < 0){
            return Msg.failError("您提交的数据有误！");
        }
        Authority authority1 = authorityService.getAuthorityByName(authority);
        if(authority1 != null){
            return Msg.success(1);//存在
        }
        return Msg.success(0);//不存在
    }

    @PostMapping("/add")
    @RequiresPermissions("authority:add")
    public Object add(Authority authority){
        if(StringUtils.isBlank(authority.getAuthName())){
            return Msg.failError("您提交的数据有误");
        }
        Authority add = authorityService.add(authority);
        if(add != null){
            return Msg.success(add);
        }
        return Msg.failError("添加失败");
    }

    @GetMapping("/getAllPage")
//    @RequiresPermissions("authority:get")
    public Object getAll(Integer pageNum, Integer pageSize){
        return Msg.success(authorityService.getAllPage(pageNum, pageSize));
    }
    @GetMapping("/getAll")
//    @RequiresPermissions("authority:get")
    public Object getAll(){
        return Msg.success(authorityService.getAll());
    }

    @GetMapping("/getLevel1")
    public Object getLevel1(){
        List<Authority> level1 = authorityService.getLevel1();
        return Msg.success(level1);
    }
    @GetMapping("/getLevel2")
    public Object getLevel2(Integer pId){
        if (pId == null || pId < 1) {
            return Msg.failError("提交的数据有误");
        }
        return Msg.success(authorityService.getLevel2(pId));
    }
    @GetMapping("/getLevel3")
    public Object getLevel3(Integer pId){
        if (pId == null || pId < 1) {
            return Msg.failError("提交的数据有误");
        }
        return Msg.success(authorityService.getLevel3(pId));
    }
}
