package org.example.practise;

import org.example.cglib.reflection.TypeParameterResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class TestImp<T> implements ITest<T>{

    @Override
    public T selectOne(String id) {
        return null;
    }

    @Override
    public <E> E selectMany2(T e) {
        return null;
    }

    @Override
    public <E> T selectMany(E e) {
        return null;
    }

    public static void main(String[] args) {

        String name = ITest.class.getName();
        System.out.println(name);

        Method[] declaredMethods = TestImp.class.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            Type result = TypeParameterResolver.resolveReturnType(declaredMethod, ITest.class);
            System.out.println("result: "+result);
        }

    }
}
