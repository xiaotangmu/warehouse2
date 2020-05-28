package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Role;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.service.RoleService;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    RoleService roleService;

    @PostMapping("/delete")
    @RequiresPermissions("role:delete")
    public Object delete(String ids){
        if(StringUtils.isNotBlank(ids)){
            List<Integer> integers = MyStrUtil.intSplit(ids, "-");
            roleService.delete(integers);
            return Msg.success("删除成功！");
        }else{
            Msg.failError("提交的数据有问题！");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("role:update")
    public Object update(Role role, String oldName, String authIds){
        System.out.println("update ..." + role);
        List<Integer> integers = MyStrUtil.intSplit(authIds, "-");
        if(integers == null || integers.size() < 1){
            return Msg.failError("您提交的数据有误！");
        }
        if (StringUtils.isNotBlank(role.getRoleName()) && role.getId() != null && role.getId() > 0 && StringUtils.isNotBlank(oldName)){

            Set<Integer> set = new HashSet<>();
            set.addAll(integers);
            System.out.println(set);
            int update = roleService.update(role, oldName,set);
            if (update == -1) {
                return Msg.noCondition("角色已经存在");
            }else if(update != 0){
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }

    @PostMapping("/find")
//    @GetMapping("/findRole")
    public Object findRole(String name){//不做分页
        return Msg.success(roleService.getRoleLikeName(name));
    }

//    @GetMapping("/getAllPage")
//    public Object getAllPage(Integer pageNum, Integer pageSize){
//        return Msg.success(roleService.getAllPage(pageNum, pageSize));
//    }

    @GetMapping("/getAll")
    public Object getAll(){
        return Msg.success(roleService.getAll());
    }

    @PostMapping("/add")
    @RequiresPermissions("role:add")
    public Object add(Role role, String authIds){//添加品牌

        System.out.println(role);

        String name = role.getRoleName();

        List<Integer> ids = MyStrUtil.intSplit(authIds, "-");

        if(ids == null || ids.size() <1){
            return Msg.failError("请先赋予权限");
        }
        //判断name 是否为空
        if(StringUtils.isBlank(role.getRoleName())){
            return Msg.noCondition("name不能为空");
        }
        //判断名字是否重复
        Role role2 = roleService.getRoleByName(name);
        if (role2 != null){
            return Msg.noCondition("该name已经存在");
        }

        Set<Integer> ids2 = new HashSet<>();
        ids2.addAll(ids);
        int add = roleService.add(role, ids2);
        if (add != 0 && add != -1) {
            if (role.getId() != null) {
                return Msg.success(role);
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
            return Msg.failError("名字不能为空！");
        }
        Role role = roleService.getRoleByName(name);
        if(role == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

}
