package com.sudy.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class Sm4 {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    private static final String ALGORITHM_NAME = "SM4";
    private static final String ALGORITHM_CBC = "SM4/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16; // IV长度
    private static final int KEY_SIZE = 128; // 密钥长度

    /**
     * 生成随机密钥
     */
    public  byte[] generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, "BC");
        kg.init(KEY_SIZE, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

    /**
     * 加密（自动生成IV并拼接到密文前）
     *
     * @param key   密钥
     * @param plain 明文
     * @return Base64编码的(IV + 密文)
     */
    public  String encryptWithIV(byte[] key, byte[] plain) throws Exception {
        // 生成随机IV
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        // 加密数据
        Cipher cipher = Cipher.getInstance(ALGORITHM_CBC, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM_NAME), new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal(plain);

        // 拼接IV和密文
        byte[] result = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(cipherText, 0, result, iv.length, cipherText.length);

        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * 解密（从Base64字符串中提取IV和密文）
     *
     * @param key       密钥
     * @param encrypted Base64编码的(IV+密文)
     * @return 明文
     */
    public  byte[] decryptWithIV(byte[] key, String encrypted) throws Exception {
        // 解码Base64
        byte[] data = Base64.getDecoder().decode(encrypted);

        // 提取IV和密文
        byte[] iv = new byte[IV_SIZE];
        byte[] cipherText = new byte[data.length - IV_SIZE];
        System.arraycopy(data, 0, iv, 0, iv.length);
        System.arraycopy(data, iv.length, cipherText, 0, cipherText.length);

        // 解密
        Cipher cipher = Cipher.getInstance(ALGORITHM_CBC, "BC");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM_NAME), new IvParameterSpec(iv));
        return cipher.doFinal(cipherText);
    }


}