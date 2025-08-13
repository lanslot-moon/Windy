package com.zj.notify.utils;


import com.zj.common.utils.OrikaUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 **/
@Slf4j
public final class SpiFactory {

    private SpiFactory() {
        throw new IllegalStateException("Utility class");
    }

    private static final Map<Class<?>, Object> cache = new HashMap<>();

    private static final Map<Class<?>, List<?>> cacheList = new HashMap<>();

    public static <T> T get(Class<T> c) {
        return get(c, null);
    }

    public static <T> List<T> getList(Class<T> c) {
        if (cacheList.get(c) != null) {
            return OrikaUtil.convertList(cacheList.get(c), c) ;
        }

        synchronized (SpiFactory.class) {
            if (cacheList.get(c) != null) {
                return OrikaUtil.convertList(cacheList.get(c), c) ;
            }

            ServiceLoader<T> load = ServiceLoader.load(c);
            List<T> list = new ArrayList<>();
            load.forEach(list::add);
            cacheList.put(c, list);
            return list;
        }
    }

    public static <T> T get(Class<T> sourceClass, T defaultClass) {
        if (cache.get(sourceClass) != null) {
            return OrikaUtil.convert(cache.get(sourceClass), sourceClass);
        }
        synchronized (SpiFactory.class) {
            if (cache.get(sourceClass) != null) {
                return OrikaUtil.convert(cache.get(sourceClass), sourceClass);
            }

            ServiceLoader<T> load = ServiceLoader.load(sourceClass);
            Iterator<T> iterator = load.iterator();
            if (!iterator.hasNext()) {
                return defaultClass;
            }

            T loadClass = iterator.next();
            cache.put(sourceClass, loadClass);
            return loadClass;
        }
    }
}
