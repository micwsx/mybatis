package org.example.practise;

import org.example.cglib.reflection.TypeParameterResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class TestImp2 implements ITest<String>{

    @Override
    public String selectOne(String id) {
        return null;
    }

    @Override
    public <E> E selectMany2(String e) {
        return null;
    }

    @Override
    public <E> String selectMany(E e) {
        return null;
    }

    public static void main(String[] args) {
        Method[] declaredMethods = TestImp2.class.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            System.out.println(declaredMethod.getName());
            Type result = TypeParameterResolver.resolveReturnType(declaredMethod, TestImp2.class);
            System.out.println("result: "+result);
        }
    }
}
