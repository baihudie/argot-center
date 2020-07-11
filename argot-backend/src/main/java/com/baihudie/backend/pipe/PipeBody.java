package com.baihudie.backend.pipe;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PipeBody {

    public static final int CON_ACTIVE = 0;

    public static final int SEND_TO_NULL = 0;
    public static final int SEND_TO_SELF = 1;
    public static final int SEND_TO_LIST = 2;
    public static final int SEND_TO_ALL = 3;

    //发送回人数类型
    private int sendTo;

    private List<MessageBody> messageBodyList = new ArrayList<>();

    public PipeBody(int sendTo) {

        this.sendTo = sendTo;
    }

    public void addMessageBody(MessageBody messageBody) {

        messageBodyList.add(messageBody);
    }

    public void addMessageBody(String pseudonym, int resType, String body) {
        MessageBody messageBody = new MessageBody();
        messageBody.setPseudonym(pseudonym);
        messageBody.setResType(resType);
        messageBody.setBody(body);

        messageBodyList.add(messageBody);

    }
}
