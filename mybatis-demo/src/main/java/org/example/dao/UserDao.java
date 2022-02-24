package org.example.dao;

import org.apache.ibatis.annotations.Param;
import org.example.model.User;

import java.util.List;

public interface UserDao {

    User getById(Integer Id);

    User getByName(String name);

    List<User> getAll();

    List<User> getCriteria(@Param("name") String name,@Param("age") Integer age);

    int add(User user);

    int test();

}
