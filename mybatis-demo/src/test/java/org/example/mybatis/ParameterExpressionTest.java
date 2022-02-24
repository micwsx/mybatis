package org.example.mybatis;

import org.apache.ibatis.builder.ParameterExpression;
import org.example.cglib.reflection.ParamNameUtil;
import org.example.dao.UserDao;
import org.junit.Test;

import java.lang.reflect.Method;

public class ParameterExpressionTest {

    @Test
    public void test(){
        try {
            Method method = UserDao.class.getMethod("getById", Integer.class);
            String paramName = ParamNameUtil.getParamNames(method).get(0);
            System.out.println(paramName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


}
