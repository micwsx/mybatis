package org.example.cglib.repo;

import org.apache.ibatis.annotations.Select;
import org.example.cglib.binding.SqlCommandAnnotation;
import org.example.model.User;

public interface UserRepo {

    @SqlCommandAnnotation(commandType = "SELECT",sqlText = "select * from user where id=?")
    User getById(Integer id);
}
