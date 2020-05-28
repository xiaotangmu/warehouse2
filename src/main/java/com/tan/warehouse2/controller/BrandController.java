package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Brand;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.service.BrandService;
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
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    BrandService brandService;

    @PostMapping("/delete")
    @RequiresPermissions("brand:delete")
    public Object delete(String names, String ids){
        if(StringUtils.isNotBlank(names) && StringUtils.isNotBlank(ids)){
            List<Integer> integers = MyStrUtil.intSplit(ids, "-");
            List<String> strings = MyStrUtil.strSplit(names, "-");
            brandService.delete(strings, integers);
            return Msg.success("删除成功！");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("authority:update")
    public Object update(Brand brand){
        System.out.println("update ..." + brand);
        if (StringUtils.isNotBlank(brand.getName()) && brand.getId() != null && brand.getId() > 0){
            int update = brandService.update(brand);
            if (update != 0) {
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }

    @PostMapping("/findBrand")
//    @GetMapping("/findBrand")
    public Object findBrand(String name, Integer pageNum, Integer pageSize){
        return Msg.success(brandService.getBrandLikeName(name, pageNum, pageSize));
    }

    @GetMapping("/getAllPage")
    public Object getAllPage(Integer pageNum, Integer pageSize){
        return Msg.success(brandService.getAllPage(pageNum, pageSize));
    }
    @GetMapping("/getAll")
    public Object getAll(){
        return Msg.success(brandService.getAll());
    }

    @GetMapping("/add")
    @RequiresPermissions("brand:add")
    public Object add(Brand brand){//添加品牌

        System.out.println(brand);

        //判断name 是否为空
        if(StringUtils.isBlank(brand.getName())){
            return Msg.noCondition("品牌名不能为空");
        }
        //判断名字是否重复
        String name = brand.getName();
        Brand brand2 = brandService.getBrandByName(name);
        if (brand2 != null){
            return Msg.noCondition("该品牌已经存在");
        }
        int add = brandService.add(brand);
        if (add != 0 && add != -1) {
            if (brand.getId() != null) {
                return Msg.success(brand);
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
        Brand brand = brandService.getBrandByName(name);
        if(brand == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

    @PostMapping("/fileUpload")
    @RequiresPermissions("brand:fileUpload")
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
}
