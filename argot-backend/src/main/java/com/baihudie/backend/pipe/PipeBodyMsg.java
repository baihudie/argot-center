package com.baihudie.backend.pipe;

import lombok.Data;

@Data
public class PipeBodyMsg extends PipeBody {

    public PipeBodyMsg(int sendTo) {
        super(sendTo);
    }


}
