package org.example.cglib.binding;
import org.example.cglib.session.SqlSession;
import javax.sql.DataSource;

public class MapperRegistry {

    public <T> T getMapper(Class<T> type, SqlSession sqlSession){
        MapperProxyFactory<T> mapperProxyFactory=new MapperProxyFactory<>(type);
        return mapperProxyFactory.newInstance(sqlSession);
    }

}
