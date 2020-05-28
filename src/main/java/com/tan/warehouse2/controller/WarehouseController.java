package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.bean.Warehouse;
import com.tan.warehouse2.service.WarehouseService;
import com.tan.warehouse2.utils.FileUploadUtil;
import com.tan.warehouse2.utils.MyImageUtils;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    WarehouseService warehouse2Service;

    @PostMapping("/delete")
    @RequiresPermissions("warehouse:delete")
    public Object delete(String names, String ids){
        if(StringUtils.isNotBlank(names) && StringUtils.isNotBlank(ids)){
            List<Integer> integers = MyStrUtil.intSplit(ids, "-");
            List<String> strings = MyStrUtil.strSplit(names, "-");
            warehouse2Service.delete(strings, integers);
            return Msg.success("删除成功！");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("warehouse:update")
    public Object update(Warehouse warehouse2, String oldName){
        System.out.println("update ..." + warehouse2);
        if (StringUtils.isNotBlank(warehouse2.getName()) && warehouse2.getId() != null && warehouse2.getId() > 0 && StringUtils.isNotBlank(oldName)){
            int update = warehouse2Service.update(warehouse2,oldName);
            if (update != 0) {
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }

    @PostMapping("/find")
//    @GetMapping("/findWarehouse")
    public Object findWarehouse(String name, Integer pageNum, Integer pageSize){
        return Msg.success(warehouse2Service.getWarehouseLikeName(name, pageNum, pageSize));
    }

    @GetMapping("/getAllPage")
    public Object getAllPage(Integer pageNum, Integer pageSize){
        return Msg.success(warehouse2Service.getAllPage(pageNum, pageSize));
    }

    @PostMapping("/add")
    @RequiresPermissions("warehous:add")
    public Object add(Warehouse warehouse2){//添加品牌

        System.out.println(warehouse2);

        //判断name 是否为空
        if(StringUtils.isBlank(warehouse2.getName())){
            return Msg.noCondition("品牌名不能为空");
        }
        //判断名字是否重复
        String name = warehouse2.getName();
        Warehouse warehouse22 = warehouse2Service.getWarehouseByName(name);
        if (warehouse22 != null){
            return Msg.noCondition("该品牌已经存在");
        }
        int add = warehouse2Service.add(warehouse2);
        if (add != 0 && add != -1) {
            if (warehouse2.getId() != null) {
                return Msg.success(warehouse2);
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
        Warehouse warehouse2 = warehouse2Service.getWarehouseByName(name);
        if(warehouse2 == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

    @PostMapping("/fileUpload")
    //参数使用bigdecimal -- 数据可能是小数，也方便后面处理
    public Object fileUpload(@RequestParam("file") MultipartFile multipartFile){
        // 将图片或者音视频上传到分布式的文件存储系统
        //图片处理（处理类可以自己编写，没有完善）
        MyImageUtils imageUtils = new MyImageUtils();
        String extName = imageUtils.cutType(multipartFile);//得到后缀名  -- 写出来方便复用

        String imgUrl = null;
        if(multipartFile != null){//上传图片到文件存储系统
            FileUploadUtil fileUploadUtil = new FileUploadUtil();
            imgUrl = fileUploadUtil.uploadImage(multipartFile, "png");
        }
        System.out.println(imgUrl);
        // 将图片的存储路径返回给页面
        return Msg.success(imgUrl);
    }

    @GetMapping("/getAll")
    public Object getAll(){

        return Msg.success(warehouse2Service.getAll());
    }
}
