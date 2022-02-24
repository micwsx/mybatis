package org.example.practise;

import org.apache.ibatis.io.Resources;
import org.example.cglib.repo.UserRepo;
import org.example.cglib.session.DefaultSqlSession;
import org.example.cglib.session.SqlSession;
import org.example.cglib.session.SqlSessionFactory;
import org.example.cglib.session.SqlSessionFactoryBuilder;
import org.example.model.User;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 * Mybatis 重写框架代码测试
 */
public class AppCustomTest {

    private SqlSession sqlSession;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void init() {
        try {
            String resource = "db.properties";
            InputStream resourceAsStream = Resources.getResourceAsStream(resource);
            SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
            SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resourceAsStream);
            sqlSession = sqlSessionFactory.openSession();
            logger.info("Initialize SqlSession successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testCustomMybatisFramework() {
        UserRepo mapper = sqlSession.getMapper(UserRepo.class);
        User user = mapper.getById(1);
        System.out.println(user);
        assertTrue(user != null);
    }
}
