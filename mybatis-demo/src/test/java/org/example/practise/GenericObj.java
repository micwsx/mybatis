package org.example.practise;

import org.example.cglib.reflection.TypeParameterResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://blog.csdn.net/baidu_19338587/article/details/103276196
public class GenericObj<T> {

    private List<T> items; // Parameterized Type
    private List<String> names; // Parameterized Type
    private GenericObj genericObj;
    private GenericObj<T> genericObj2; // Parameterized Type
    private List list;
    private T t; // type variable
    private Map<T, T> map;

    public GenericObj() {

    }

    private <E> E getItem(T t, E e) {
        return e;
    }

    public GenericObj<T> test(List<T> item, GenericObj<Integer> g, T t) {
        return null;
    }

    public static void main(String[] args) {
        printFieldsInfo();
//        printMethodsInfo();

    }


    private static void printMethodsInfo() {
        StringBuffer stringBuffer = new StringBuffer();
        Method[] declaredMethods = GenericObj.class.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            stringBuffer.append("methodName: " + declaredMethod.getName()).append("\r\n");
            Type[] genericParameterTypes = declaredMethod.getGenericParameterTypes();
            for (Type genericParameterType : genericParameterTypes) {
                stringBuffer.append("genericParameterType: " + genericParameterType).append("\r\n");
            }

            TypeVariable<Method>[] typeParameters = declaredMethod.getTypeParameters();

            for (TypeVariable<Method> typeParameter : typeParameters) {
                stringBuffer.append("name: " + typeParameter.getName()).append("\r\n");
                stringBuffer.append("bounds: " + Arrays.asList(typeParameter.getBounds())).append("\r\n");
                stringBuffer.append("typeParameters: " + typeParameters).append("\r\n");
            }

            Class<?>[] parameterTypes = declaredMethod.getParameterTypes();

            for (Class<?> parameterType : parameterTypes) {
                stringBuffer.append("parameterType: " + parameterType).append("\r\n");
            }

            Class<?> returnType = declaredMethod.getReturnType();
            stringBuffer.append("returnType: " + returnType).append("\r\n");

            Type genericReturnType = declaredMethod.getGenericReturnType();
            stringBuffer.append("genericReturnType: " + genericReturnType).append("\r\n");

            stringBuffer.append("-----\r\n");
        }
        System.out.println(stringBuffer.toString());
    }

    private static void printFieldsInfo() {
        StringBuffer stringBuffer = new StringBuffer();
//        Field[] fields = GenericObj.class.getFields();
        Field[] declaredFields = GenericObj.class.getDeclaredFields();
        System.out.println("declaredFields length:" + declaredFields.length);
        for (Field declaredField : declaredFields) {
            Type genericType = declaredField.getGenericType();
            // 指定类型泛型<String>或者没有指定泛型<T>都是ParameterizedType
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type rawType = parameterizedType.getRawType();
                Type ownerType = parameterizedType.getOwnerType();
                // 获取参数化数组，有可能定义了多个类型参数
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Class<? extends ParameterizedType> aClass = parameterizedType.getClass();
                stringBuffer.append("ParameterizedType: ").append("\r\n");
                stringBuffer.append("rawType: " + rawType).append("\r\n");
                stringBuffer.append("ownerType: " + ownerType).append("\r\n");
                stringBuffer.append("class: " + aClass.getName()).append("\r\n");

                if (null != actualTypeArguments) {
                    for (Type actualTypeArgument : actualTypeArguments) {
                        stringBuffer.append("actualTypeArgument: " + actualTypeArgument).append("\r\n");
                    }
                }
            } else if (genericType instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) genericType;
                String name = typeVariable.getName();
                Type[] bounds = typeVariable.getBounds();
                GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
                TypeVariable<?>[] typeParameters = genericDeclaration.getTypeParameters();
                stringBuffer.append("TypeVariable: ").append("\r\n");
                stringBuffer.append("name: " + name).append("\r\n");
                stringBuffer.append("bounds: " + Arrays.asList(bounds)).append("\r\n");
                stringBuffer.append("genericDeclaration: " + genericDeclaration).append("\r\n");
                stringBuffer.append("typeParameters: " + Arrays.asList(typeParameters)).append("\r\n");
            } else {
                stringBuffer.append("genericType: " + genericType).append("\r\n");
            }
            stringBuffer.append("--------------\r\n");
        }

        System.out.println(stringBuffer.toString());
    }

}
