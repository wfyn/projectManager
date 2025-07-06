package com.sudy.common;

import cn.hutool.http.HttpStatus;


import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName:   R
 * @Description: 返回数据
 * @author: Ming
 * @date:   2018年12月3日 上午10:28:39
 */
public class Result extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public Result() {
        put("code", 200);
        put("msg", "success");
    }

    public static Result error() {
        return error(HttpStatus.HTTP_BAD_REQUEST, "未知异常，请联系管理员");
    }

    public static Result error(String msg) {
        return error(HttpStatus.HTTP_BAD_REQUEST, msg);
    }

    public static Result error(int code, String msg) {
        Result result = new Result();
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }

//    public static Result ok(String msg) {
//        Result result = new Result();
//        result.put("msg", msg);
//        return result;
//    }

    public static Result ok(Map<String, Object> map) {
        Result result = new Result();
        result.putAll(map);
        return result;
    }

    public static Result ok() {
        return new Result();
    }

    public static Result ok(Object data) {
        Result result = new Result();
        result.put("data",data);
        return result;
    }

    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}