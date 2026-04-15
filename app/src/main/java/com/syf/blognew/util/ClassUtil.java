package com.syf.blognew.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yfsun10
 * @version 1.0
 * @date 2021/5/18 15:31
 */
public class ClassUtil {
    public static Map<String,Object> getNameValue(Object o){
        Map<String,Object> result=new HashMap<>();
        Class<?> mClass=o.getClass();
        for(Field field:mClass.getDeclaredFields()){
            field.setAccessible(true);
            Method m= null;
            try {
                m = o.getClass().getMethod("get"+getMethodName(field.getName()));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            Object val= null;
            try {
                assert m != null;
                val = m.invoke(o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if(val!=null){
                result.put(field.getName(),val);
            }
        }
        return result;
    }

    private static String getMethodName(String fildeName) {
        byte[] items = fildeName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        return new String(items);
    }
}
