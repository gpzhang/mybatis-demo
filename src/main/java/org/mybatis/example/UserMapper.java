package org.mybatis.example;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author haishen
 * @date 2018/5/7
 */
public interface UserMapper {
    User selectUserById(Long id);

    @Select(value = " select * from User where name = #{name}")
    User selectUserByName(String name);


    List<User> selectUserByAge(@Param("age") Integer age);

    List<User> listByCondition(@Param("page") PageCondition page, @Param("id") Long id);


}
