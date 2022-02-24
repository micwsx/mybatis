package org.example.cglib.reflection;

import java.lang.reflect.*;
import java.util.Arrays;

public class TypeParameterResolver {

    public static Type resolveFieldType(Field field, Type srcType) {
        Type genericType = field.getGenericType();
        Class<?> declaringClass = field.getDeclaringClass();
        return resolveType(genericType, srcType, declaringClass);
    }

    public static Type resolveReturnType(Method method, Type srcType){
        Type returnType = method.getGenericReturnType();
        Class<?> declaringClass = method.getDeclaringClass();
        return resolveType(returnType,srcType,declaringClass);
    }

    /**
     * resolve param types
     * @param method
     * @param srcType
     * @return
     */
    public static Type[] resolveParamTypes(Method method,Type srcType){
        Type[] parameterTypes = method.getGenericParameterTypes();
        Class<?> declaringClass = method.getDeclaringClass();
        Type[] result=new Type[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            result[i]=resolveType(parameterTypes[i],srcType,declaringClass);
        }
        return result;
    }

    private static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
        if (type instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            return resolveTypeVar(typeVariable, srcType, declaringClass);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return resolveParameterizedType(parameterizedType, srcType, declaringClass);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return resolveGenericArrayType(genericArrayType, srcType, declaringClass);
        } else {
            return type;
        }
    }


    /**
     * 泛型数组 e.g. T[]
     *
     * @param genericArrayType
     * @param srcType
     * @param declaringClass
     * @return
     */
    private static Type resolveGenericArrayType(GenericArrayType genericArrayType, Type srcType, Class<?> declaringClass) {
        Type genericComponentType = genericArrayType.getGenericComponentType();
        Type resolvedComponentType=null;
        if (genericComponentType instanceof TypeVariable){
            resolvedComponentType=resolveTypeVar((TypeVariable<?>)genericComponentType,srcType,declaringClass);
        }else if(genericArrayType instanceof GenericArrayType){
            resolvedComponentType=resolveGenericArrayType((GenericArrayType) genericArrayType,srcType,declaringClass);
        }else if(genericArrayType instanceof ParameterizedType){
            resolvedComponentType=resolveParameterizedType((ParameterizedType)genericComponentType,srcType,declaringClass);
        }
        if (resolvedComponentType instanceof Class){
            return Array.newInstance((Class<?>) resolvedComponentType,0).getClass();
        }else{
            return new GenericArrayTypeImpl(resolvedComponentType);
        }
    }


    /***
     * 参数化类型  e.g. List<T> v1, List<String> v2
     * @param parameterizedType
     * @param srcType
     * @param declaringClass
     * @return
     */
    private static Type resolveParameterizedType(ParameterizedType parameterizedType, Type srcType, Class<?> declaringClass) {
        Class<?> rawType = (Class<?>)parameterizedType.getRawType();
        Type[] typeArgs = parameterizedType.getActualTypeArguments();
        Type[] args=new Type[typeArgs.length];
        for (int i = 0; i < typeArgs.length; i++) {
            if (typeArgs[i] instanceof TypeVariable){
                TypeVariable  typeVariable=(TypeVariable) typeArgs[i];
                args[i]=resolveTypeVar(typeVariable,srcType,declaringClass);
            }else if (typeArgs[i] instanceof ParameterizedType){
                args[i]=resolveParameterizedType((ParameterizedType) typeArgs[i],srcType,declaringClass);
            }else if(typeArgs[i] instanceof  WildcardType){
                args[i]=resolveWildcardType((WildcardType) typeArgs[i],srcType,declaringClass);
            }else{
                args[i]=typeArgs[i];
            }
        }
        return new ParameterizedTypeImpl(rawType,null,args);
    }

    private static Type resolveWildcardType(WildcardType wildcardType, Type srcType,Class<?> declaringClass){
        Type[] lowerBounds=resolveWildcardTypeBounds(wildcardType.getLowerBounds(),srcType,declaringClass);
        Type[] upperBounds=resolveWildcardTypeBounds(wildcardType.getUpperBounds(),srcType,declaringClass);
        return new WildcardTypeImpl(lowerBounds,upperBounds);
    }

    private static Type[] resolveWildcardTypeBounds(Type[] bounds, Type srcType, Class<?> declaringClass){
        Type[] result=new Type[bounds.length];
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i] instanceof TypeVariable) {
                result[i] = resolveTypeVar((TypeVariable<?>) bounds[i], srcType, declaringClass);
            } else if (bounds[i] instanceof ParameterizedType) {
                result[i] = resolveParameterizedType((ParameterizedType) bounds[i], srcType, declaringClass);
            } else if (bounds[i] instanceof WildcardType) {
                result[i] = resolveWildcardType((WildcardType) bounds[i], srcType, declaringClass);
            } else {
                result[i] = bounds[i];
            }
        }
        return result;
    }

    /***
     * 类型变量. e.g. T variable
     * @param typeVariable
     * @param srcType
     * @param declaringClass
     * @return
     */
    private static Type resolveTypeVar(TypeVariable<?> typeVariable, Type srcType, Class<?> declaringClass) {
        Type result;
        Class<?> clazz;
        if (srcType instanceof Class) {
            clazz = (Class<?>) srcType;
        } else if (srcType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) srcType;
            clazz = (Class<?>) parameterizedType.getRawType();
        } else {
            throw new IllegalArgumentException("The 2nd arg must be Class or ParameterizedType, but was: " + srcType.getClass());
        }

        if (clazz == declaringClass) {
            Type[] bounds = typeVariable.getBounds();
            if (bounds.length > 0) {
                return bounds[0];
            }
            return Object.class;
        }

        // 找这个类的父类类型（只能有一个父类）
        Type superclass = clazz.getGenericSuperclass();
        result=scanSuperTypes(typeVariable,srcType,declaringClass,clazz,superclass);
        if (result!=null){
            return result;
        }

        // 找这个类的实现接口类型数组(可以实现了多个接口)
        Type[] superInterfaces= clazz.getGenericInterfaces();
        for (Type superInterface : superInterfaces) {
            result=scanSuperTypes(typeVariable,srcType,declaringClass,clazz,superInterface);
            if (result!=null){
                return result;
            }
        }
        return Object.class;
    }

    private static Type scanSuperTypes(TypeVariable<?> typeVar, Type srcType, Class<?> declaringClass, Class<?> clazz, Type superclass) {
        if (superclass instanceof ParameterizedType){
            ParameterizedType parentAsType = (ParameterizedType) superclass;
            Class<?> parentAsClass = (Class<?>)parentAsType.getRawType();
            TypeVariable<? extends Class<?>>[] parentTypeVars = parentAsClass.getTypeParameters();
            if (srcType instanceof ParameterizedType){
                parentAsType = translateParentTypeVars((ParameterizedType) srcType, clazz, parentAsType);
            }
            if (declaringClass == parentAsClass){
                for (int i = 0; i < parentTypeVars.length; i++) {
                    if (typeVar.equals(parentTypeVars[i])){
                        return parentAsType.getActualTypeArguments()[i];
                    }
                }
            }
            if (declaringClass.isAssignableFrom(parentAsClass)){
                return  resolveTypeVar(typeVar,parentAsType,declaringClass);
            }
        }else if(superclass instanceof Class && declaringClass.isAssignableFrom((Class<?>)superclass)){
            return  resolveTypeVar(typeVar,superclass,declaringClass);
        }
        return  null;
    }

    private static ParameterizedType translateParentTypeVars(ParameterizedType srcType, Class<?> srcClass, ParameterizedType parentType) {
        Type[] parentTypeArgs = parentType.getActualTypeArguments();
        Type[] srcTypeArgs = srcType.getActualTypeArguments();
        TypeVariable<?>[] srcTypeVars = srcClass.getTypeParameters();
        Type[] newParentArgs = new Type[parentTypeArgs.length];
        boolean noChange = true;
        for (int i = 0; i < parentTypeArgs.length; i++) {
            if (parentTypeArgs[i] instanceof TypeVariable) {
                for (int j = 0; j < srcTypeVars.length; j++) {
                    if (srcTypeVars[j].equals(parentTypeArgs[i])) {
                        noChange = false;
                        newParentArgs[i] = srcTypeArgs[j];
                    }
                }
            } else {
                newParentArgs[i] = parentTypeArgs[i];
            }
        }
        return noChange ? parentType : new ParameterizedTypeImpl((Class<?>)parentType.getRawType(), null, newParentArgs);
    }

    static class ParameterizedTypeImpl implements ParameterizedType {
        private Class<?> rawType;

        private Type ownerType;

        private Type[] actualTypeArguments;

        public ParameterizedTypeImpl(Class<?> rawType, Type ownerType, Type[] actualTypeArguments) {
            super();
            this.rawType = rawType;
            this.ownerType = ownerType;
            this.actualTypeArguments = actualTypeArguments;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public String toString() {
            return "ParameterizedTypeImpl [rawType=" + rawType + ", ownerType=" + ownerType + ", actualTypeArguments=" + Arrays.toString(actualTypeArguments) + "]";
        }
    }

    static class WildcardTypeImpl implements WildcardType{

        private Type[] lowerBounds;
        private Type[] upperBounds;

        public WildcardTypeImpl(Type[] lowerBounds, Type[] upperBounds) {
            this.lowerBounds = lowerBounds;
            this.upperBounds = upperBounds;
        }

        @Override
        public Type[] getUpperBounds() {
            return upperBounds;
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBounds;
        }
    }

    static class GenericArrayTypeImpl implements GenericArrayType {
        private Type genericComponentType;

        GenericArrayTypeImpl(Type genericComponentType) {
            super();
            this.genericComponentType = genericComponentType;
        }

        @Override
        public Type getGenericComponentType() {
            return genericComponentType;
        }
    }




}
