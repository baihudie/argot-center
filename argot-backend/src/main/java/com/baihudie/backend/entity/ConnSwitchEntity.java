package com.baihudie.backend.entity;

import lombok.Data;

@Data
public class ConnSwitchEntity {

    public static final int OK = 1;

    // init is 0
    private int tcp1;
    private int tcp2;

    private int tcp3;
    private int tcp4;


    private String token;

    // A
    private String aPseudonym;
    private String aBanditCode;

    private String aRemoteIp;
    private int aRemotePort;

    // B
    private String bPseudonym;
    private String bBanditCode;

    private String bRemoteIp;
    private int bRemotePort;

    public boolean validate(BanditEntity aBanditEntity, BanditEntity bBanditEntity) {

        if (aBanditEntity == null) {
            return false;
        }

        if (bBanditEntity == null) {
            return false;
        }

        if (!aBanditEntity.getBanditCode().equals(aBanditCode)) {
            return false;
        }

        if (!aBanditEntity.getPseudonym().equals(aPseudonym)) {
            return false;
        }

        if (!bBanditEntity.getBanditCode().equals(bBanditCode)) {
            return false;
        }
        if (!bBanditEntity.getPseudonym().equals(bPseudonym)) {
            return false;
        }

        return true;
    }
}
