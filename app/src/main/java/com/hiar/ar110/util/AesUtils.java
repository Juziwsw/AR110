package com.hiar.ar110.util;

import android.util.Log;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author wsw
 */
public class AesUtils {
    /**
     * 加解密算法/工作模式/填充模式
     */
    private static final String CIPHER_MODE = "AES/CBC/NoPadding";
    /**
     * 默认编码
     */
    private static final String CHARSET = "utf-8";

    /**
     * 加密
     * @param data 待加密数据
     * @param key   密钥(偏移量和密钥用的同一值，可设置和密钥不同的值)
     */
    public static String encrypt(String data, String key) {
        String ivString = key;
        // 偏移量--可自行设置和key不相同的值
        byte[] iv = ivString.getBytes();
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            int blockSize = cipher.getBlockSize();
            byte[] dataBytes = data.getBytes(CHARSET);
            int length = dataBytes.length;

            // 计算需填充长度
            if (length % blockSize != 0) {
                length = length + (blockSize - (length % blockSize));
            }
            byte[] plaintext = new byte[length];
            // 填充0,实现ZeroPadding
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(CHARSET), "AES");
            //设置偏移量参数
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryped = cipher.doFinal(plaintext);

            return new String(java.util.Base64.getEncoder().encode(encryped));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密
     * @param data 待加密数据
     * @param key   密钥(偏移量和密钥用同一值)
     */
    public static String desEncrypt(String data,String key) {
        try {
            // 偏移量
            byte[] iv = key.getBytes();

            // 密钥
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(CHARSET), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 初始工具
            Cipher instance = Cipher.getInstance(CIPHER_MODE);
            // DECRYPT_MODE 解密模式
            instance.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] encryptMsgBytes = Base64.getDecoder().decode(data);

            byte[] doFinal = instance.doFinal(encryptMsgBytes);
            // 去除填充的0,获取填充0的起始位置，删除填充的0
            int zeroIndex = doFinal.length;
            for (int i = doFinal.length - 1; i > 0; i--) {
                if (doFinal[i] == (byte) 0) {
                    zeroIndex = i;
                } else {
                    break;
                }
            }
            // 删除末尾填充的0
            doFinal = Arrays.copyOf(doFinal, zeroIndex);

            return new String(doFinal, CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
