package com.liferay.bluetooth;

public class Convert {

    private Convert(){}

    public static byte[] hexStringToByteArray(String string) {
        int len = string.length();

        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character
                    .digit(string.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteToDecimalString(byte value) {
        return String.valueOf(value);
    }

    public static String byteToAscii(byte value) {
        char[] data = new char[1];

        data[0] = (char) value;

        return new String(data);
    }
}
