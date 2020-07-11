package com.baihudie.api.proto.body;

import lombok.Data;

import java.util.List;

@Data
public class QueryAllResBody {

    private List<QueryAllResBodySub> pseudonymList;

}


