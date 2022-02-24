package org.example.cglib.binding;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.example.cglib.mapping.SqlCommandType;
import org.example.cglib.reflection.ParamNameResolver;
import org.example.cglib.reflection.TypeParameterResolver;
import org.example.cglib.session.SqlSession;

import java.lang.reflect.*;
import java.util.*;

public class MapperMethod {

    private final SqlCommand sqlCommand;

    private final MethodSignature methodSignature;

    public MapperMethod(Class<?> mapperInterface, Method method, String sqlText) {
        this.sqlCommand = new SqlCommand(mapperInterface, method);
        this.methodSignature = new MethodSignature(mapperInterface, method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        // check the sql statement.
        switch (sqlCommand.getSqlCommandType()) {
            case INSERT: {
                Object param = methodSignature.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.insert(sqlCommand.getSqlText(), param));
                break;
            }
            case UPDATE: {
                Object param = methodSignature.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.update(sqlCommand.getSqlText(), param));
                break;
            }
            case DELETE: {
                Object param = methodSignature.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.delete(sqlCommand.getSqlText(), param));
                break;
            }
            case SELECT: {
                if (methodSignature.returnsVoid() && methodSignature.hasResultHandler()) {
                    // TODO: callable statement with ResultHandler.
                    result = null;
                } else if (methodSignature.returnsMany()) {
                    result = executeForMany(sqlSession, args);
                } else if (methodSignature.returnsMap()) {
                    result=executeForMap(sqlSession,args);
                } else {
                    Object param = methodSignature.convertArgsToSqlCommandParam(args);
                    result = sqlSession.selectOne(sqlCommand.getSqlText(), param);
                    if (methodSignature.returnsOptional()
                            && (result == null || !methodSignature.getReturnType().equals(result.getClass()))) {
                        result = Optional.ofNullable(result);
                    }
                }
                break;
            }
            default:
                throw new BindingException("Unknown execution method for: " + this.sqlCommand.getName());
        }
        if (result == null && methodSignature.getReturnType().isPrimitive()
                && !methodSignature.returnsVoid()) {
            throw new BindingException("Mapper method '" + sqlCommand.getName()
                    + " attempted to return null from a method with a primitive return type (" + methodSignature.getReturnType() + ").");
        }
        return result;
    }

    private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
        Map<K, V> result;
        Object param = methodSignature.convertArgsToSqlCommandParam(args);
        if (methodSignature.hasRowBounds()) {
            RowBounds rowBounds = methodSignature.extractRowBounds(args);
            result = sqlSession.selectMap(sqlCommand.getSqlText(), param, methodSignature.getMapKey(), rowBounds);
        } else {
            result = sqlSession.selectMap(sqlCommand.getSqlText(), param, methodSignature.getMapKey());
        }
        return result;
    }

    private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
        List<E> result;
        Object param = methodSignature.convertArgsToSqlCommandParam(args);
        if (methodSignature.hasRowBounds()) {
            RowBounds rowBounds = methodSignature.extractRowBounds(args);
            result = sqlSession.selectList(sqlCommand.getSqlText(), param, rowBounds);
        } else {
            result = sqlSession.selectList(sqlCommand.getSqlText(), param);
        }
        if (!methodSignature.getReturnType().isAssignableFrom(result.getClass())) {
            if (methodSignature.getReturnType().isArray()) {
                return convertToArray(result);
            } else {
                return convertToDeclaredCollection(result);
            }
        }
        return result;
    }

    private <E> Object convertToDeclaredCollection(List<E> list) {
        DefaultObjectFactory objectFactory = new DefaultObjectFactory();
        Object collection = objectFactory.create(methodSignature.getReturnType());
        ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        MetaObject metaObject = MetaObject.forObject(collection, objectFactory, objectWrapperFactory, reflectorFactory);
        metaObject.addAll(list);
        return collection;
    }

    private <E> Object convertToArray(List<E> list) {
        Class<?> componentType = methodSignature.getReturnType().getComponentType();
        Object array = Array.newInstance(componentType, list.size());
        if (componentType.isPrimitive()) {
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        } else {
            return list.toArray((E[]) array);
        }
    }


    private Object rowCountResult(int rowCount) {
        final Object result;
        if (methodSignature.returnsVoid()) {
            result = null;
        } else if (Integer.class.equals(methodSignature.getReturnType())
                || Integer.TYPE.equals(methodSignature.getReturnType())) {
            result = rowCount;
        } else if (Long.class.equals(methodSignature.getReturnType())
                || Long.TYPE.equals(methodSignature.getReturnType())) {
            result = (long) rowCount;
        } else if (Boolean.class.equals(methodSignature.getReturnType()) ||
                Boolean.TYPE.equals(methodSignature.getReturnType())) {
            result = rowCount > 0;
        } else {
            throw new BindingException("Mapper method '" + sqlCommand.getName() + "' has an unsupported return type: " + methodSignature.getReturnType());
        }
        return result;
    }


    /**
     * get
     * SQL command type(SELECT/UPDATE etc.)
     * sqlText
     * name: statement Id
     * based on declared class and target method.
     *
     */
    public static class SqlCommand {
        private final SqlCommandType sqlCommandType;
        private final String sqlText;
        private final String name;// statement id;

        public SqlCommand(Class<?> mapperInterface, Method method) {
            String methodName = method.getName();
            SqlCommandAnnotation annotation = method.getAnnotation(SqlCommandAnnotation.class);
            sqlCommandType = SqlCommandType.valueOf(annotation.commandType());
            sqlText = annotation.sqlText();
            this.name = resolveMappedStatement(mapperInterface, methodName, method.getDeclaringClass());
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getSqlCommandType() {
            return sqlCommandType;
        }

        public String getSqlText() {
            return sqlText;
        }

        private String resolveMappedStatement(Class<?> mapperInterface, String methodName, Class<?> declaringClass) {
            // className[full name]+"."+methodName
            String smId = mapperInterface.getName() + "." + methodName;
            return smId;
        }

    }

    public static class MethodSignature {

        private final boolean returnsMany;
        private final boolean returnsMap;
        private final boolean returnsVoid;
        private final boolean returnsOptional;
        private final Class<?> returnType;
        private final String mapKey;
        private final Integer resultHandlerIndex;
        private final Integer rowBoundsIndex;
        // <传入的参数名，值>
        private final ParamNameResolver paramNameResolver;

        public MethodSignature(Class<?> mapperInterface, Method method) {
            Type resolveReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
            if (resolveReturnType instanceof Class<?>) {
                this.returnType = (Class<?>) resolveReturnType;
            } else if (resolveReturnType instanceof ParameterizedType) {
                this.returnType = (Class<?>) ((ParameterizedType) resolveReturnType).getRawType();
            } else {
                this.returnType = method.getReturnType();
            }
            this.returnsVoid = void.class.equals(this.returnType);
            this.returnsMany = Collection.class.isAssignableFrom(this.returnType)
                    || this.returnType.isArray();
            this.returnsOptional = Optional.class.equals(this.returnType);
            this.mapKey = getMapKey(method);
            this.returnsMap = this.mapKey != null;
            this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
            this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
            this.paramNameResolver = new ParamNameResolver(method);
        }

        public Object convertArgsToSqlCommandParam(Object[] args) {
            return paramNameResolver.getNamedParams(args);
        }


        public boolean hasRowBounds() {
            return this.rowBoundsIndex != null;
        }

        public RowBounds extractRowBounds(Object[] args) {
            return hasRowBounds() ? (RowBounds) args[this.rowBoundsIndex] : null;
        }

        public boolean hasResultHandler() {
            return resultHandlerIndex != null;
        }

        public ResultHandler extractResultHandler(Object[] args) {
            return hasResultHandler() ? (ResultHandler) args[resultHandlerIndex] : null;
        }

        public Class<?> getReturnType() {
            return returnType;
        }

        public boolean returnsMany() {
            return returnsMany;
        }

        public boolean returnsMap() {
            return returnsMap;
        }

        public boolean returnsVoid() {
            return returnsVoid;
        }

        public boolean returnsOptional() {
            return returnsOptional;
        }

        private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
            Integer index = null;
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (paramType.isAssignableFrom(argTypes[i])) {
                    if (index == null) {
                        index = i;
                    } else {
                        throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
                    }
                }
            }
            return index;
        }

        public String getMapKey() {
            return mapKey;
        }

        private String getMapKey(Method method) {
            String mapKey = null;
            if (Map.class.isAssignableFrom(method.getReturnType())) {
                final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
                if (mapKeyAnnotation != null) {
                    mapKey = mapKeyAnnotation.value();
                }
            }
            return mapKey;
        }


    }

    public static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }

}
