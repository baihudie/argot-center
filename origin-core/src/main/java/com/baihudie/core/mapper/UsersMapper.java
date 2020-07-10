package com.baihudie.core.mapper;

import com.baihudie.core.model.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface UsersMapper {

    int insertUsers(Users users);

    Users getUsersById(
            @Param("id") Integer id);

}
