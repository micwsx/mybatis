package org.example.practise;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeFactory {

    /**
     * 获取unsafe对象
     * @return
     */
    public static Unsafe getUnsafe(){
        try {

            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);
            return unsafe;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取对象变量偏移地址
     * @param unsafe
     * @param clazz
     * @param fieldName
     * @return
     */
    public static long  getFieldOffset(Unsafe unsafe, Class clazz,String fieldName){
        Field field = null;
        try {
            field = clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return unsafe.objectFieldOffset(field);
    }


}
