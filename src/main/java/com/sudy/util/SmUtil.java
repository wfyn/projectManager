package com.sudy.util;


import cn.hutool.core.io.FileUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 国密算法工具类（SM3/SM4）
 * 注意：生产环境应避免硬编码密钥，建议从安全配置或KMS系统获取
 */
public class SmUtil {


    // 硬编码的SM4密钥（Base64格式） - 仅用于演示，实际项目应从安全存储获取
    private static  String SM4_KEY = "";

    private static final Sm3 Sm3 = new Sm3();

    private static final Sm4 Sm4 = new Sm4();

    /**
     * SM3哈希计算（字符串输入）
     *
     * @param input 原始字符串
     * @return 64位十六进制哈希值
     */
    public static String sm3encrypt(String input) {
        if (input == null) {
            throw new IllegalArgumentException("SM3输入不能为null");
        }
        return Sm3.hash(input);
    }

    /**
     * SM3哈希计算（字节数组输入）
     *
     * @param input 二进制数据
     * @return 64位十六进制哈希值
     */
    public static String sm3encrypt(byte[] input) {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("SM3输入不能为空");
        }
        return Sm3.hash(input);
    }

    /**
     * SM4加密（使用类内置密钥）
     *
     * @param input 明文数据
     * @return Base64编码的(IV + 密文)
     * @throws Exception 加密失败时抛出异常
     */
    public static String sm4encrypt(String input) throws Exception {
        byte[] key = getDefaultKey();
        return Sm4.encryptWithIV(key, input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SM4加密（自定义密钥）
     *
     * @param key   16字节SM4密钥
     * @param input 明文数据
     * @return Base64编码的(IV + 密文)
     * @throws Exception 加密失败时抛出异常
     */
    public static String sm4encrypt(byte[] key, String input) throws Exception {
        validateKey(key);
        return Sm4.encryptWithIV(key, input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SM4解密（使用类内置密钥）
     *
     * @param input Base64编码的(IV+密文)
     * @return 原始明文数据
     * @throws Exception 解密失败时抛出异常
     */
    public static String sm4decrypt(String input) throws Exception {
        byte[] key = getDefaultKey();
        return new String(Sm4.decryptWithIV(key, input), StandardCharsets.UTF_8);
    }

    /**
     * SM4解密（自定义密钥）
     *
     * @param key   16字节SM4密钥
     * @param input Base64编码的(IV+密文)
     * @return 原始明文数据
     * @throws Exception 解密失败时抛出异常
     */
    public static byte[] sm4decrypt(byte[] key, String input) throws Exception {
        validateKey(key);
        return Sm4.decryptWithIV(key, input);
    }

    // 获取默认密钥（实际项目应从安全存储获取）
    private static byte[] getDefaultKey() throws FileNotFoundException {
        if (SM4_KEY != null && !SM4_KEY.isEmpty()) {
            return Base64.getDecoder().decode(SM4_KEY);
        }
        File file = ResourceUtils.getFile("classpath:sm4/sm4.key");
        if (!file.exists()) {
            throw new FileNotFoundException("SM4密钥文件不存在");
        }
        SM4_KEY = new String(FileUtil.readBytes(file), StandardCharsets.UTF_8);
        return Base64.getDecoder().decode(SM4_KEY);
    }

    // 验证密钥有效性
    private static void validateKey(byte[] key) {
        if (key == null || key.length != 16) {
            throw new IllegalArgumentException("SM4密钥必须为16字节");
        }
    }
    public static String readFile(String filePath) {
        // 获取输入流（适合读取非文件系统资源，比如jar包内文件）
        try (InputStream is = new ClassPathResource(filePath).getInputStream()) {
            byte[] bytes = FileCopyUtils.copyToByteArray(is);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {

        }
        return null;
    }

}