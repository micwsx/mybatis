package org.example;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try {
            SqlSessionFactoryBuilder sqlSessionFactoryBuilder=new SqlSessionFactoryBuilder();

            Reader reader=new FileReader(new File("classpath:/mybatis.xml"));
            SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(reader);
            SqlSession sqlSession = sqlSessionFactory.openSession();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println( "Hello World!" );
    }
}
