package com.baihudie.backend.pipe;

import lombok.Data;

@Data
public class PipeBodyCon extends PipeBody {



    //控制类型
    private int conType;

    //内部控制消息
    private ControlBody controlBody;

    //是否关闭连接
    private boolean closeCtx;

    public PipeBodyCon(int sendTo) {
        super(sendTo);
    }

}
