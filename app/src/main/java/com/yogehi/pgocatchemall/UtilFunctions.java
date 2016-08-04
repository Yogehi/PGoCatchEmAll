package com.yogehi.pgocatchemall;

public class UtilFunctions {
    private static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1',
            (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
            (byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
            (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' };

    public static int getBit(byte byteValue, int position)
    {
        return (byteValue >> position) & 1;
    }

    public static byte[] getByteFromHexStr(String str)
    {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = (byte) Integer
                    .parseInt(str.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }


    public static String getHexString(byte[] raw) {
        int len = raw.length;
        byte[] hex = new byte[2 * len];
        int index = 0;
        int pos = 0;

        for (byte b : raw) {
            if (pos >= len)
                break;

            pos++;
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }

        return new String(hex);
    }

    public static String getHexString(int number){

        String hexStr = Integer.toHexString(number).toUpperCase();
        if(hexStr.length() % 2 != 0)
            hexStr = "0" + hexStr;

        return hexStr;
    }

    public static String getAsciiFromHex(byte[] bytes){

        String strAscii = "";

        for(int i = 0; i < bytes.length; i++){
            if((int)bytes[i] > 31 && (int)bytes[i] < 127){
                byte[] tmp = {bytes[i]};
                strAscii += new String(tmp);
            } else{
                strAscii += ".";
            }
        }
        return strAscii;
    }

    public static byte[] concatByteArray(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
}
