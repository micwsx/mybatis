package org.example.cglib.reflection;

import org.apache.ibatis.annotations.Param;
import org.example.cglib.binding.MapperMethod.ParamMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class ParamNameResolver {
    public static final String GENERIC_NAME_PREFIX = "param";
    // mybatis configuration默认是true
    private static boolean useActualParamNam = true;
    private final SortedMap<Integer, String> names;
    private boolean hasParamAnnotation;

    public ParamNameResolver(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 因为参数前可以添加多个注解,所以是二维数组
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        SortedMap<Integer, String> map = new TreeMap<>();
        int paramCount = parameterAnnotations.length;
        // get names from @Param annotations
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            String name = null;
            for (Annotation annotation : parameterAnnotations[paramIndex]) {
                if (annotation instanceof Param) {
                    hasParamAnnotation = true;
                    name = ((Param) annotation).value();
                    break;
                }
            }
            if (name == null) {
                // @Param was not specified.
                if (useActualParamNam) {
                    name = getActualParamName(method, paramIndex);
                }
                if (name == null) {
                    // use the parameter index as the name ("0", "1", ...)
                    name = String.valueOf(map.size());
                }
            }
            map.put(paramIndex, name);
        }
        names = Collections.unmodifiableSortedMap(map);
    }

    private String getActualParamName(Method method, int paramIndex) {
        return ParamNameUtil.getParamNames(method).get(paramIndex);
    }

    public String[] getNames() {
        return names.values().toArray(new String[0]);
    }

    public Object getNamedParams(Object[] args) {
        final int paramCount = names.size();
        if (args == null || paramCount == 0) {
            return null;
        } else if (!hasParamAnnotation && paramCount == 1) {
            Object value = args[names.firstKey()];
            return wrapToMapIfCollection(value, useActualParamNam ? names.get(0) : null);
        } else {
            // <参数名,参数值>
            final Map<String, Object> param = new ParamMap<>();
            int i=0;
            for (Map.Entry<Integer, String> entry : names.entrySet()) {
                param.put(entry.getValue(),args[entry.getKey()]);
                final String genericParamName=GENERIC_NAME_PREFIX+(i+1);
                if (!names.containsValue(genericParamName)){
                    param.put(genericParamName,args[entry.getKey()]);
                }
                i++;
            }
            return param;
        }

    }

    public static Object wrapToMapIfCollection(Object object, String actualParamName) {
        if (object instanceof Collection) {
            ParamMap<Object> map = new ParamMap<>();
            map.put("collection", object);
            if (object instanceof List) {
                map.put("list", object);
            }
            Optional.ofNullable(actualParamName).ifPresent(name -> map.put(name, object));
            return map;
        } else if (object != null && object.getClass().isArray()) {
            ParamMap<Object> map = new ParamMap<>();
            map.put("array", object);
            Optional.ofNullable(actualParamName).ifPresent(name -> map.put(name, object));
            return map;
        }
        return object;
    }


}
