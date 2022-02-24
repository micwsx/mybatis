package org.example.cglib.session;

import org.apache.ibatis.session.RowBounds;

import java.io.Closeable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface SqlSession extends Closeable {

    <T> T selectOne(String statement);

    <T> T selectOne(String statement,Object parameter);

    <E> List<E> selectList(String statement);

    <E> List<E> selectList(String statement,Object parameter);

    <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);


    int insert(String statement, Object parameter);

    int update(String statement, Object parameter);

    <T> T getMapper(Class<T> type);

    Connection getConnection();

    int delete(String statement, Object parameter);

    <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey);

    <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds);



}
