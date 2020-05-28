package com.tan.warehouse2.utils;

import org.apache.http.entity.ContentType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.*;
import java.math.BigDecimal;

public class MyImageUtils {

    /**
     * 几种常见的图片格式
     */
    public static String IMAGE_TYPE_GIF = "gif";// 图形交换格式
    public static String IMAGE_TYPE_JPG = "jpg";// 联合照片专家组
    public static String IMAGE_TYPE_JPEG = "jpeg";// 联合照片专家组
    public static String IMAGE_TYPE_BMP = "bmp";// 英文Bitmap（位图）的简写，它是Windows操作系统中的标准图像文件格式
    public static String IMAGE_TYPE_PNG = "png";// 可移植网络图形
    public static String IMAGE_TYPE_PSD = "psd";// Photoshop的专用格式Photoshop

    /**
     * 图像切割(按指定起点坐标和宽高切割)
     *
     * @param x            目标切片起点坐标X
     * @param y            目标切片起点坐标Y
     * @param width        目标切片宽度
     * @param height       目标切片高度
     * @param extName       图片后缀
     */
    public MultipartFile cut(MultipartFile multipartFile, int x, int y, int width, int height, String extName) {
        try {
            // 读取源图像
//            FileInputStream in = (FileInputStream) multipartFile.getInputStream();//会报错
//            System.out.println(multipartFile.getOriginalFilename());//为null
            InputStream in = multipartFile.getInputStream();
            BufferedImage src = ImageIO.read(in); // 读入文件
            int width0 = src.getWidth(); // 得到源图宽
            int height0 = src.getHeight(); // 得到源图长
            if (width0 > 0 && height0 > 0) {
                Image image = src.getScaledInstance(width0, height0,
                        Image.SCALE_DEFAULT);
                // 四个参数分别为图像起点坐标和宽高
                // 即: CropImageFilter(int x,int y,int width,int height)
                ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
                Image img = Toolkit.getDefaultToolkit().createImage(
                        new FilteredImageSource(image.getSource(),
                                cropFilter));
                BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

                Graphics g = tag.getGraphics();
                g.drawImage(img, 0, 0, null); // 绘制缩小后的图
                g.dispose();

                // 输出为文件
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(tag, extName, os);// 输出到文件流
                InputStream input = new ByteArrayInputStream(os.toByteArray());
                MultipartFile file = new MockMultipartFile(ContentType.APPLICATION_OCTET_STREAM.toString(), input);
//                System.out.println(file.getBytes().length);
                return file;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 缩放图像（按比例缩放）
     *
     * @param multipartFile 前端传过来的图片
     * @param scale         缩放比例
     */
    public MultipartFile scale(MultipartFile multipartFile, BigDecimal scale) {
        try {
//            FileInputStream in = (FileInputStream) multipartFile.getInputStream();//不报错 -- 统一配置
            InputStream in = multipartFile.getInputStream();
            BufferedImage src = ImageIO.read(in); // 读入文件
            int width = src.getWidth(); // 得到源图宽
            int height = src.getHeight(); // 得到源图长
            System.out.println("origin: " + width + " * " +height);

            System.out.println(scale);
            BigDecimal w = new BigDecimal(width);
            BigDecimal h = new BigDecimal(height);

            // 缩放
            width = w.multiply(scale).intValue();
            height = h.multiply(scale).intValue();
            System.out.println("scaled: " + width + " * " +height);

            Image image = src.getScaledInstance(width, height,
                    Image.SCALE_DEFAULT);
            BufferedImage tag = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);

            Graphics g = tag.getGraphics();
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(tag, cutType(multipartFile), os);// 输出到文件流
//            byte[] bytes = os.toByteArray();
//            return bytes;
            InputStream input = new ByteArrayInputStream(os.toByteArray());
//            InputStream inputStream = new ByteArrayInputStream(testFile);
            MultipartFile file = new MockMultipartFile(ContentType.APPLICATION_OCTET_STREAM.toString(), input);
//            System.out.println(file.getBytes().length);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //截取文件后缀 -- 类型
    public String cutType(MultipartFile multipartFile){
        // 获得文件后缀名
        String originalFilename = multipartFile.getOriginalFilename();// a.jpg
        int i = originalFilename.lastIndexOf(".");
        return originalFilename.substring(i + 1);
    }


    /**
     * 缩放图像（按比例缩放） -- 用来测试使用
     *
     * @param scale        缩放比例
     */
    public void scale1(MultipartFile multipartFile, BigDecimal scale, int x, int y, int scaleWidth, int scaleHeight) {
        try {
            InputStream in = multipartFile.getInputStream();
            BufferedImage src = ImageIO.read(in); // 读入文件
            int width = src.getWidth(); // 得到源图宽

            int height = src.getHeight(); // 得到源图长
            System.out.println("原图： " + width + " * " + height);

            BigDecimal w = new BigDecimal(width);
            BigDecimal h = new BigDecimal(height);

            // 缩放
            width = w.multiply(scale).intValue();
            height = h.multiply(scale).intValue();
            System.out.println("scaled: " + width + " * " +height);

            Image image = src.getScaledInstance(width, height,
                    Image.SCALE_DEFAULT);
//            BufferedImage tag = new BufferedImage(width, height,
//                    BufferedImage.TYPE_INT_RGB);

            ImageFilter cropFilter = new CropImageFilter(x, y, scaleWidth, scaleHeight);
            Image img = Toolkit.getDefaultToolkit().createImage(
                    new FilteredImageSource(image.getSource(),
                            cropFilter));
            BufferedImage tag = new BufferedImage(scaleWidth, scaleHeight, BufferedImage.TYPE_INT_RGB);
            //绘制图片，没有图片会只剩黑图
            Graphics g = tag.getGraphics();
            g.drawImage(img, 0, 0, null); // 绘制缩小后的图
            g.dispose();

            ImageIO.write(tag, "JPEG", new File("d:/a.jpg"));// 输出到文件流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
