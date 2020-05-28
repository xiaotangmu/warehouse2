package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.service.UserService;
import com.tan.warehouse2.utils.FileUploadUtil;
import com.tan.warehouse2.utils.MyImageUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Controller
public class TestController {

    @Autowired
    UserService userService;

    @PostMapping("fileUpload")
    @ResponseBody
    //参数使用bigdecimal -- 数据可能是小数，也方便后面处理
    public Object fileUpload(@RequestParam("file") MultipartFile multipartFile, BigDecimal Xstart, BigDecimal Ystart,
                             BigDecimal Worigin, BigDecimal Wnow, String name){
        System.out.println(Xstart + " : " + Ystart);
        System.out.println(Worigin + " : " + Wnow);
        System.out.println("name" + " : " + name);
        if(StringUtils.isBlank(name)){
            return Msg.failError("提交的数据有误");
        }
        // 将图片或者音视频上传到分布式的文件存储系统
        //图片处理（处理类可以自己编写，没有完善）
        MyImageUtils imageUtils = new MyImageUtils();
        BigDecimal scale = Wnow.divide(Worigin, 5 , BigDecimal.ROUND_HALF_UP);//缩放倍数 现在的图片大小/原来的图片大小 -- 保留5为小数 -- 四舍五入
        MultipartFile file2 = imageUtils.scale(multipartFile, scale);//缩放图片 -- 返回file 便于应付各种需求
        String extName = imageUtils.cutType(multipartFile);//得到后缀名  -- 写出来方便复用
        file2 = imageUtils.cut(file2, Xstart.intValue(), Ystart.intValue(), 150, 150, extName);//裁剪图片

        //在windows保存测试
//        imageUtils.scale1(multipartFile, b.divide(a, 5 , BigDecimal.ROUND_HALF_UP), Xstart.intValue() ,Ystart.intValue(),150,150);

        String imgUrl = null;
        if(file2 != null){//上传图片到文件存储系统
            FileUploadUtil fileUploadUtil = new FileUploadUtil();
            imgUrl = fileUploadUtil.uploadImage(file2, "png");
        }
        System.out.println(imgUrl);
        //更新数据库
        userService.updatePhoto(name, imgUrl);

        // 将图片的存储路径返回给页面
        return Msg.success(imgUrl);
    }
}
