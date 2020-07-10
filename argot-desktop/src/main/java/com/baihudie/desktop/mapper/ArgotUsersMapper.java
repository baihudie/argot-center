package com.baihudie.desktop.mapper;

import com.baihudie.desktop.model.ArgotUsers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface ArgotUsersMapper {

    int insertArgotUsers(ArgotUsers users);

    ArgotUsers getArgotUsersById(
            @Param("id") Integer id);

}
