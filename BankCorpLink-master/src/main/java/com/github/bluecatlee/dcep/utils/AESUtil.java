package com.github.bluecatlee.dcep.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AESUtil {

	private static final String KEY_ALGORITHM = "AES";
	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";// 默认的加密算法

	/**
	 * AES 加密操作
	 * 
	 * @param strSrc
	 *            待加密内容
	 * @param key
	 *            加密密钥
	 * @return 返回Base64转码后的加密数据
	 */
	public static String encrypt(String strSrc, String key) {
		try {
			Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);// 创建密码器

			byte[] byteContent = strSrc.getBytes("utf-8");

			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key));// 初始化为加密模式的密码器

			byte[] result = cipher.doFinal(byteContent);// 加密

			String re = new String(Base64.encode(result), "utf-8");// 通过Base64转码返回
			return re;
		} catch (Exception ex) {
			Logger.getLogger(AESUtil.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	/**
	 * AES 解密操作
	 * 
	 * @param strSrc
	 * @param key
	 * @return
	 */
	public static String decrypt(String strSrc, String key) {

		try {
			// 实例化
			Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

			// 使用密钥初始化，设置为解密模式
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key));

			// 执行操作
			//byte[] result = cipher.doFinal((new BASE64Decoder()).decodeBuffer(strSrc));
			byte[] result = cipher.doFinal(Base64.decode(strSrc.getBytes("utf-8")));
			//byte[] result = cipher.doFinal(Base64.decodeBase64(strSrc.getBytes()));
			return new String(result, "utf-8");
		} catch (Exception ex) {
			Logger.getLogger(AESUtil.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	/**
	 * 生成加密秘钥
	 * 
	 * @return
	 */
	private static SecretKeySpec getSecretKey(final String key) throws UnsupportedEncodingException {
		// 返回生成指定算法密钥生成器的 KeyGenerator 对象
		KeyGenerator kg = null;

		try {
			kg = KeyGenerator.getInstance(KEY_ALGORITHM);
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(key.getBytes("utf-8"));
			// AES 要求密钥长度为 128
			kg.init(128, secureRandom);

			// 生成一个密钥
			SecretKey secretKey = kg.generateKey();

			return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);// 转换为AES专用密钥
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(AESUtil.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	public static void main(String[] args) {
		String strSrc = "china counstruciton bank,ccb,中国建设银行";
		String key = "sde*&h9dfn9ODSHofdlkjiohi*&#(*&C(*NOSD*Sdeersde*&h9dfn9ODSHofdlkjiohi*&#(*&C(*NOSD*Sdeer";
		System.out.println("strSrc:" + strSrc);
		String encryptedResult = AESUtil.encrypt(strSrc, key);
		System.out.println("encryptedResult:" + encryptedResult);
		System.out.println("decryptedResult:" + AESUtil.decrypt(encryptedResult, key));

	}

}