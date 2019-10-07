// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.util;

import java.io.File;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.BitSet;

public class Utils {
    public static final char[] HEX = "0123456789ABCDEF".toCharArray();

    public static byte[] reverseBytes(byte[] input) {
        int length = input.length;

        byte[] output = new byte[length];
        for (int i = 0; i < length; i++) {
            output[i] = input[length - (i + 1)];
        }

        return output;
    }

    public static byte[] decodeHex(String hex) {
        if (hex.length() % 2 == 1) {
            hex = "0" + hex;
        }
        int len = hex.length();
        
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    public static String encodeHex(byte[] bytes) {
        /* Two hex characters always represent one byte */
        char[] hex = new char[bytes.length << 1];
        for (int i = 0, j = 0; i < bytes.length; i++) {
            hex[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
            hex[j++] = HEX[(0x0F & bytes[i])];
        }
        return new String(hex);
    }

    public static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
        return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
    }

    public static byte[] toBytes(BigInteger value, int size) {
        byte[] dest = new byte[size];
        byte[] src = value.toByteArray();

        int length = Math.min(src.length, size);
        int destPos = Math.max(size - src.length, 0);
        System.arraycopy(src, 0, dest, destPos, length);

        return dest;
    }

    public static byte[] trimmedByteArrayFromInteger(int input) {
        return trimmedByteArrayFromLong(input);
    }

    public static byte[] trimmedByteArrayFromLong(long input) {
        int x = 8;
        do {
            if ((input >> ((x - 1) * 8)) != 0) {
                break;
            }
            x--;
        } while (x > 1);

        byte[] trimmedByteArray = new byte[x];
        for (int i = 0; i < x; i++) {
            trimmedByteArray[x - i - 1] = (byte)(input);
            input >>= 8;
        }

        return trimmedByteArray;
    }

    public static String leftPad(char[] initial, char pad, int length) {
        Preconditions.argument(initial.length <= length, "Invalid pad length");

        char[] chars = new char[length];
        int offset = length - initial.length;
        for (int i = 0; i < length; i++) {
            if (i >= offset) {
                chars[i] = initial[i - offset];
            } else {
                chars[i] = pad;
            }
        }

        return new String(chars);
    }

    public static String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        int pos = name.lastIndexOf(".");

        return pos == -1 ? name : name.substring(0, pos);
    }

    public static int getCurrentTimestamp() {
        return (int)Instant.now().getEpochSecond();
    }

    public static boolean verifySignature(byte[] payload, byte[] signatureBytes, byte[] publicKeyBytes) {
        try {
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = KeyFactory.getInstance("EC").generatePublic(publicKeySpec);
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(payload);

            return signature.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    public static int toInt(BitSet bitset) {
        int value = 0;
        for (int i = 0; i < bitset.length(); i++) {
            if (bitset.get(i)) {
                value += 1 << i;
            }
        }
        return value;
    }

    public static int toInt(byte[] bytes) {
        if (bytes.length > 4) throw new IllegalArgumentException();

        byte[] intBytes = new byte[4];
        System.arraycopy(bytes, 0, intBytes, 4 - bytes.length, bytes.length);

        return ByteBuffer.wrap(intBytes).getInt();
    }

    public static byte[] toByteArray(int value) {
        return new byte[]{(byte)((value & -16777216) >> 24), (byte)((value & 16711680) >> 16), (byte)((value & '\uff00') >> 8), (byte)(value & 255)};
    }

    public static long toLong(byte[] bytes) {
        if (bytes.length > 8) throw new IllegalArgumentException();

        byte[] intBytes = new byte[8];
        System.arraycopy(bytes, 0, intBytes, 8 - bytes.length, bytes.length);

        return ByteBuffer.wrap(intBytes).getLong();
    }
    
    public static byte[] fillBytes(byte value, int count) {
        byte[] data = new byte[count];
        for(int i = 0; i < count; i++) data[i] = value;
        return data;
    }

    public static class Bytes {
        public static short readBEInt16(ByteBuffer buffer) {
            return buffer.getShort();
        }

        public static int readBEInt32(ByteBuffer buffer) {
            return buffer.getInt();
        }

        public static int readLEInt32(ByteBuffer buffer) {
            return Integer.reverseBytes(buffer.getInt());
        }

        public static void putBEInt16(ByteBuffer buffer, short value) {
            buffer.putShort(value);
        }

        public static void putBEInt32(ByteBuffer buffer, int value) {
            buffer.putInt(value);
        }

        public static void putBEBytes(ByteBuffer buffer, byte[] value) {
            buffer.put(value);
        }

        public static void putLEBytes(ByteBuffer buffer, byte[] value) {
            buffer.put(reverseBytes(value));
        }

        public static void putLEInt32(ByteBuffer buffer, int value) {
            buffer.putInt(Integer.reverseBytes(value));
        }
    }
}
