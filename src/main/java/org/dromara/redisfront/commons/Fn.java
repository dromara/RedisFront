package org.dromara.redisfront.commons;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;


/**
 * 常用函数
 *
 * @author Jin
 */
public class Fn {

    private Fn() {
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return CollectionUtil.isNotEmpty(collection);
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return MapUtil.isNotEmpty(map);
    }

    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    public static boolean isNotEmpty(final Object array) {
        return ArrayUtil.isNotEmpty(array);
    }

    public static boolean isNotNull(Object obj) {
        return ObjectUtil.isNotNull(obj);
    }

    public static boolean isNull(Object obj) {
        return ObjectUtil.isNull(obj);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return CollectionUtil.isEmpty(collection);
    }

    public static <T> boolean isEmpty(T[] array) {
        return ArrayUtil.isEmpty(array);
    }

    public static <T> T[] isEmpty(T[] array, T[] defaultValue) {
        return ArrayUtil.isEmpty(array) ? defaultValue : array;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return MapUtil.isEmpty(map);
    }

    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    public static boolean equal(Object obj1, Object obj2) {
        return ObjectUtil.equal(obj1, obj2);
    }

    public static boolean notEqual(Object obj1, Object obj2) {
        return ObjectUtil.notEqual(obj1, obj2);
    }

    public static boolean startWith(String str, String prefix) {
        return StrUtil.startWith(str, prefix);
    }

    public static boolean endsWith(String str, String suffix) {
        return StrUtil.endWith(str, suffix);
    }

    public static String toJson(Object obj) {
        var s = JSONUtil.parse(obj).toStringPretty();
        return JSONUtil.parse(obj).toStringPretty();
    }


    public static int getByteSize(Object data) {
        int byteSize = 0;
        try (var byteArrayOutputStream = new ByteArrayOutputStream(); var os = new ObjectOutputStream(byteArrayOutputStream)) {
            os.writeObject(data);
            byteSize = byteArrayOutputStream.size();
        } catch (Exception e) {
            return byteSize;
        }
        return byteSize;
    }

    public static String getDataSize(String str) {
        if (isNotEmpty(str)) {
            return DataSizeUtil.format(str.getBytes().length);
        }
        return DataSizeUtil.format(0);
    }


}
