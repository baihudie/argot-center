package com.baihudie.api.proto.body;

import lombok.Data;

@Data
public class InviteAcceptToResBody {

//    public static final int ACCEPT = 0;
//    public static final int DENY = 1;

    //通过好友申请
    private String rabblePseudonym;

    private int acceptStatus;

}
