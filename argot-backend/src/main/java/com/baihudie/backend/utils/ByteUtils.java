package com.baihudie.backend.utils;

public class ByteUtils {

    public static byte[] mergeByteArr(byte[] arrA, byte[] arrB) {

        byte[] arrC = new byte[arrA.length + arrB.length];


        System.arraycopy(arrA, 0, arrC, 0, arrA.length);
        System.arraycopy(arrB, 0, arrC, arrA.length, arrB.length);

        return arrC;


    }

    public static byte[] trunByteArr(byte[] arrA, int headLength) {

        byte[] arrC = new byte[arrA.length - headLength];

        System.arraycopy(arrA, headLength, arrC, 0, arrA.length - headLength);

        return arrC;


    }

}
