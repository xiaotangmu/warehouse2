package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.bean.Supplier;
import com.tan.warehouse2.service.SupplierService;
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
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    SupplierService supplierService;

    @PostMapping("/delete")
    @RequiresPermissions("supplier:delete")
    public Object delete(String names, String ids){
        if(StringUtils.isNotBlank(names) && StringUtils.isNotBlank(ids)){
            List<Integer> integers = MyStrUtil.intSplit(ids, "-");
            List<String> strings = MyStrUtil.strSplit(names, "-");
            supplierService.delete(strings, integers);
            return Msg.success("删除成功！");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("supplier:update")
    public Object update(Supplier supplier){
        System.out.println("update ..." + supplier);
        if (StringUtils.isNotBlank(supplier.getName()) && supplier.getId() != null && supplier.getId() > 0){
            int update = supplierService.update(supplier);
            if (update != 0) {
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }

    @PostMapping("/find")
//    @GetMapping("/findSupplier")
    public Object findSupplier(String name, Integer pageNum, Integer pageSize){
        return Msg.success(supplierService.getSupplierLikeName(name, pageNum, pageSize));
    }

    @GetMapping("/getAllPage")
    public Object getAllPage(Integer pageNum, Integer pageSize){
        return Msg.success(supplierService.getAllPage(pageNum, pageSize));
    }
    @GetMapping("/getAll")
    public Object getAll(){
        return Msg.success(supplierService.getAll());
    }

    @PostMapping("/add")
    @RequiresPermissions("supplier:add")
    public Object add(Supplier supplier){//添加品牌

        System.out.println(supplier);

        //判断name 是否为空
        if(StringUtils.isBlank(supplier.getName())){
            return Msg.noCondition("品牌名不能为空");
        }
        //判断名字是否重复
        String name = supplier.getName();
        Supplier supplier2 = supplierService.getSupplierByName(name);
        if (supplier2 != null){
            return Msg.noCondition("该品牌已经存在");
        }
        int add = supplierService.add(supplier);
        if (add != 0 && add != -1) {
            if (supplier.getId() != null) {
                return Msg.success(supplier);
            }
        }else if (add == -1){
            return Msg.noCondition("该品牌已经存在");
        }
        return Msg.failError("添加失败！");
    }

    @PostMapping("/nameCheck")
//    @GetMapping("/nameCheck")
    public Object nameCheck(String name){
        System.out.println(name);
        if (StringUtils.isBlank(name)) {
            return Msg.failError("品牌名不能为空！");
        }
        Supplier supplier = supplierService.getSupplierByName(name);
        if(supplier == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

}
