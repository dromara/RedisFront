package com.redisfront.commons.func;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 常用函数
 *
 * @author Jin
 */
public class Fn {

    private Fn() {
    }

    public static void revalidateAndRepaintAllFramesAndDialogs() {
        FlatLaf.revalidateAndRepaintAllFramesAndDialogs();
    }

    public static void removeAllComponent(JComponent component) {
        for (Component c : component.getComponents()) {
            component.remove(c);
        }
        Fn.revalidateAndRepaintAllFramesAndDialogs();
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

    public static boolean startWithIgnoreCase(String str, String prefix) {
        return StrUtil.startWithIgnoreCase(str, prefix);
    }

    public static boolean startWith(String str, String prefix) {
        return StrUtil.startWith(str, prefix);
    }

    public static boolean endsWith(String str, String suffix) {
        return StrUtil.endWith(str, suffix);
    }

    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return StrUtil.endWithIgnoreCase(str, suffix);
    }

    public static void copyProperties(Object source, Object target) {
        BeanUtil.copyProperties(source, target);
    }

    public static List<String> str2List(String str, String errorMsg) {
        if (isEmpty(str)) {
            throw new IllegalArgumentException(errorMsg);
        }
        if (!str.contains(",")) {
            return Collections.singletonList(str);
        }
        return Arrays.asList(str.split(","));
    }

    public static List<String> str2List(String str) {
        return str2List(str, "字符串转列表 - 参数错误");
    }

    public static String list2Str(Collection<String> collection) {
        if (isEmpty(collection)) {
            throw new IllegalArgumentException("列表转字符串 - 参数错误");
        }
        return collection.stream().map(Object::toString).collect(Collectors.joining(","));
    }


    public static String list2Str(List<Object> list) {
        if (isEmpty(list)) {
            throw new IllegalArgumentException("列表转字符串 - 参数错误");
        }
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
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
            e.printStackTrace();
        }
        return byteSize;
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return JSONUtil.toBean(json, type);
    }

    /**
     * 遍历集合，根据提供的function生成或者获取对象，汇总去重，返回列表
     *
     * @param dataList 数据集合
     * @param function 获取的列
     * @param <R>      返回的类型
     * @param <T>      数据集合的类型
     * @return
     */
    public static <R, T> List<R> collectList(List<T> dataList, Function<? super T, ? extends R> function) {
        if (Fn.isEmpty(dataList)) {
            return Collections.emptyList();
        }
        return dataList.stream().map(function).filter(Fn::isNotNull).distinct().collect(Collectors.toList());
    }

    /**
     * 遍历集合，以提供的function获取到每一个数据的key，以提供的获取数据的function获取到每一个value，默认出现相同的数据保持不变
     * 将 List&lt;A&gt; 转换为 Map&lt;A.prop,A&gt;
     *
     * @param dataList
     * @param keyMapper 获取key的映射器
     * @param <R>
     * @param <T>
     * @return
     */
    public static <R, T> Map<R, T> collectMap(List<T> dataList, Function<? super T, ? extends R> keyMapper) {
        if (Fn.isEmpty(dataList)) {
            return new HashMap<>();
        }
        return dataList.stream().collect(Collectors.toMap(keyMapper, Function.identity(), (a, b) -> a));
    }


    public static String getDataSize(String str) {
        if (isNotEmpty(str)) {
            return DataSizeUtil.format(str.getBytes().length);
        }
        return DataSizeUtil.format(0);
    }


}
