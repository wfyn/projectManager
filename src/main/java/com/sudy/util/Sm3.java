package com.sudy.util;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.security.Security;
import java.nio.charset.StandardCharsets;

public class Sm3 {

    static {
        // 注册 BouncyCastle 提供者
        Security.addProvider(new BouncyCastleProvider());
    }
    /**
     * 计算 SM3 哈希值
     *
     * @param input 原始字符串
     * @return 64位十六进制哈希值
     */
    public  String hash(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        return hash(data);
    }

    /**
     * 计算 SM3 哈希值
     *
     * @param data 字节数组
     * @return 64位十六进制哈希值
     */
    public  String hash(byte[] data) {
        SM3Digest digest = new SM3Digest();
        digest.update(data, 0, data.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return Hex.toHexString(result).toUpperCase();
    }


}