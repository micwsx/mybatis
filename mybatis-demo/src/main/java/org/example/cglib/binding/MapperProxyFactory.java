package org.example.cglib.binding;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.example.cglib.repo.UserRepo;
import org.example.cglib.session.SqlSession;
import org.example.model.User;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/***
 * 使用JDK动态代码创建接口对象
 * @param <T>
 */
public class MapperProxyFactory<T>  {

    private final Class<T> mapperInterface;

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    protected T newInstance(MapperProxy<T> mapperProxy){
       return (T)Proxy.newProxyInstance(mapperInterface.getClassLoader(),
                new Class[]{mapperInterface},mapperProxy);
    }

    public T newInstance(SqlSession sqlSession){
        MapperProxy<T> mapperProxy=new MapperProxy<>(sqlSession,this.mapperInterface);
        return newInstance(mapperProxy);
    }
}
