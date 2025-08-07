package org.example.monitor.services;

import java.util.concurrent.ConcurrentHashMap;

public class GlobalCache {
    // 使用ConcurrentHashMap保证线程安全
    private static final ConcurrentHashMap<String, Object> cacheMap = new ConcurrentHashMap<>();
    
    // 私有构造防止实例化
    private GlobalCache() {}
    
    // 获取缓存值
    public static Object get(String key) {
        return cacheMap.get(key);
    }
    
    // 设置缓存值
    public static void put(String key, Object value) {
        cacheMap.put(key, value);
    }
    
    // 删除缓存值
    public static void remove(String key) {
        cacheMap.remove(key);
    }
    
    // 判断是否存在key
    public static boolean contains(String key) {
        return cacheMap.containsKey(key);
    }
    
    // 清空缓存
    public static void clear() {
        cacheMap.clear();
    }
}