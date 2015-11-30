package com.liferay.bluetooth;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class Convert {

    private Convert() {
    }

    public static byte[] stringToByteArray(String value) {
        byte[] hex = new byte[0];

        try {

            hex = Hex.decodeHex(value.toCharArray());

            return hex;

        } catch (DecoderException decoderException) {

            System.out.println("decoderException " + decoderException);

        }

        return hex;
    }

    public static String byteToDecimalString(byte value) {
        return String.valueOf(value);
    }

    public static String byteToAscii(byte value) {
        return String.format("%02X ", value);
    }

    public static int byteToDecimal(byte value) {

        return value & 0xff;
    }

}
