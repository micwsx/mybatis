package org.example;


import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.example.dao.UserDao;
import org.example.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class MyBatisTest {

    private SqlSession sqlSession;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void init() {
        try {
            String resource = "mybatis.xml";
            InputStream resourceAsStream = Resources.getResourceAsStream(resource);
            SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
            SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resourceAsStream);
            sqlSession = sqlSessionFactory.openSession();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void close(){
        sqlSession.close();
    }

    @Test
    public void test(){
        UserDao mapper = sqlSession.getMapper(UserDao.class);
        int value= mapper.test();
        logger.info(String.valueOf(value));
    }

    @Test
    public void queryUser(){
        UserDao mapper = sqlSession.getMapper(UserDao.class);
        User user = mapper.getById(2);
//        System.out.println(user);
        logger.info(user.toString());
    }

    @Test
    public void testUser() {
        UserDao userDao = sqlSession.getMapper(UserDao.class);
        User user = new User();
        user.setName("Michael");
        user.setAddress("Shanghai");
        user.setAge(18);
        user.setBirthDate(new Date(1999, 05, 04));
        int result = userDao.add(user);
        if (result > 0)
            logger.info("add user successfully.");
        else
            logger.info("failed to add user.");
        sqlSession.commit();
    }
}
