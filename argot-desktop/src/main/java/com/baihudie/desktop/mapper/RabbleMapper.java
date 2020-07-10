package com.baihudie.desktop.mapper;

import com.baihudie.desktop.model.ArgotUsers;
import com.baihudie.desktop.model.Rabble;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface RabbleMapper {

    int insertRabble(Rabble rabble);

    Rabble getRabbleById(
            @Param("id") Integer id);

}
