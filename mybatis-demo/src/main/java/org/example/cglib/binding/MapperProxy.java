package org.example.cglib.binding;

import org.example.cglib.session.SqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MapperProxy<T> implements InvocationHandler {

    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        // 执行具体方法,其实就是执行MapperMethod对象的execute方法
        return  cachedInvoker(method).invoke(o,method,objects,sqlSession);
    }

    /**
     * encapsulate the a method to a PlainMethodInvoker object.
     * @param method
     * @return
     * @throws Throwable
     */
    private MapperMethodInvoker cachedInvoker(Method method) throws Throwable{
        // encapsulate a method to a MapperMethod,
        // then transfer to a PlainMethodInvoker object.
         return new PlainMethodInvoker(new MapperMethod(mapperInterface,method,""));
    }

    interface MapperMethodInvoker{
        Object invoke(Object proxy,Method method,Object[] args,SqlSession sqlSession) throws Throwable;
    }

    private static class PlainMethodInvoker implements MapperMethodInvoker{
        private final MapperMethod mapperMethod;

        public PlainMethodInvoker(MapperMethod mapperMethod) {
            this.mapperMethod = mapperMethod;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
            // 开始执行方法
            return mapperMethod.execute(sqlSession,args);
        }
    }
}
