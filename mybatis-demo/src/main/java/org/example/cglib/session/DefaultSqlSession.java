package org.example.cglib.session;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.result.DefaultMapResultHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.example.cglib.binding.MapperRegistry;
import org.example.cglib.reflection.ParamNameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class DefaultSqlSession implements SqlSession {

    private final Logger logger= LoggerFactory.getLogger(getClass());
    private final boolean autoCommit;
    private Executor executor;

    public DefaultSqlSession(boolean autoCommit, Executor executor) {
        this.executor=executor;
        this.autoCommit = autoCommit;
    }

    public DefaultSqlSession(Executor executor) {
      this(false,executor);
    }


    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        List<T> list = this.selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return this.selectList(statement, null);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.selectList(statement, parameter, RowBounds.DEFAULT);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return selectList(statement, parameter, rowBounds, null);
    }

    private <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        try{
            // TODO 执行SQL语句 参数
//            MappedStatement ms = new MappedStatement.Builder().build();
            logger.info("sql: "+ statement);
            logger.info("param: "+ parameter);
            List<Object> result = executor.query(null, wrapCollection(parameter), rowBounds, handler);
            return null;
        }catch (Exception e){
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        }
    }

    @Override
    public int insert(String statement, Object parameter) {
        return 0;
    }

    @Override
    public int update(String statement, Object parameter) {
        return 0;
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        MapperRegistry mapperRegistry = new MapperRegistry();
        return mapperRegistry.getMapper(type, this);
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    private Object wrapCollection(final Object object) {
        return ParamNameResolver.wrapToMapIfCollection(object, null);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public int delete(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return selectMap(statement, parameter, mapKey, RowBounds.DEFAULT);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        final List<? extends V> list = selectList(statement, parameter, rowBounds);
        ObjectFactory objectFactory = new DefaultObjectFactory();
        ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        final DefaultMapResultHandler<K, V> mapResultHandler = new DefaultMapResultHandler<>(mapKey,
                objectFactory, objectWrapperFactory, reflectorFactory);
        final DefaultResultContext<V> context = new DefaultResultContext<>();
        for (V o : list) {
            context.nextResultObject(o);
            mapResultHandler.handleResult(context);
        }
        return mapResultHandler.getMappedResults();
    }
}
