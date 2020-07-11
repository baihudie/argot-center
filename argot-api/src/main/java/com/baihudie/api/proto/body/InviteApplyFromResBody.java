package com.baihudie.api.proto.body;

import lombok.Data;

@Data
public class InviteApplyFromResBody {

    public static final int RESULT_SEND_TO = 0;

    public static final int RESULT_NOT_SET = 1;

    private String toPseudonym;

    private int inviteResult;

}
