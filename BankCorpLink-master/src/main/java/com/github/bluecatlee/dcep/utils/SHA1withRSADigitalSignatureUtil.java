package com.github.bluecatlee.dcep.utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class SHA1withRSADigitalSignatureUtil {	
	// 用sha1生成内容摘要，再用RSA的私钥加密，进而生成数字签名
	public static String genSha1Sign(String strSrc, PrivateKey privateKey) throws Exception {
		byte[] contentBytes = strSrc.getBytes("utf-8");
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initSign(privateKey);
		signature.update(contentBytes);
		byte[] signs = signature.sign();
		return Base64.encodeBuffer(signs);
	}

	// 用sha1生成内容摘要，再用RSA的私钥加密，进而生成数字签名
	public static String genSha1Sign(String strSrc, String privateHexKey) throws Exception {	
		 PrivateKey privateKey = RSAKeyPairUtil.getPrivateKey(privateHexKey);
		 return genSha1Sign(strSrc, privateKey);
	}

	// 对用md5和RSA私钥生成的数字签名进行验证
	public static boolean verifySHA1withRSASign(String strSrc, String sign, PublicKey publicKey) throws Exception {
		byte[] contentBytes = strSrc.getBytes("utf-8");
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initVerify(publicKey);
		signature.update(contentBytes);
		return signature.verify(Base64.decodeBuffer(sign));
	}

	// 对用md5和RSA私钥生成的数字签名进行验证
	public static boolean verifySHA1withRSASign(String strSrc, String sign, String publicHexKey) throws Exception {
		PublicKey publicKey = RSAKeyPairUtil.getPublicKey(publicHexKey);
		
		byte[] contentBytes = strSrc.getBytes("utf-8");
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initVerify(publicKey);
		signature.update(contentBytes);
		return signature.verify(Base64.decodeBuffer(sign));
	}
	
}