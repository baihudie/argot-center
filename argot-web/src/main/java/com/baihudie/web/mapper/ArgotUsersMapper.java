package com.baihudie.web.mapper;

import com.baihudie.web.model.ArgotUsers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface ArgotUsersMapper {

    int insertArgotUsers(ArgotUsers users);

    ArgotUsers getArgotUsersById(
            @Param("id") Integer id);

}
