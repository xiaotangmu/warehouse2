package com.tan.warehouse2.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @date: 2020-03-30 10:32:49
 * @author: Tan.WL
 */
public class MyStrUtil {

    public static List<String> strSplit(String strs, String splitStr){
        String[] split = strs.split(splitStr);
        List<String> list = new ArrayList<>();
        for (String s : split) {
         list.add(s);
        }
        return list;
    }

    public static List<Integer> intSplit(String intStr, String splitStr){
        String[] split = intStr.split(splitStr);
        List<Integer> list = new ArrayList<>();
        for (String s : split) {
            list.add(Integer.parseInt(s));
        }
        return list;
    }

}
