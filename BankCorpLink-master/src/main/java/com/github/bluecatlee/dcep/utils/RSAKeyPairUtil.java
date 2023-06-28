package com.github.bluecatlee.dcep.utils;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAKeyPairUtil {
	public static final String KEY_ALGORITHM = "RSA";

	private static final char[] bcdLookup = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	public RSAKeyPairUtil() throws Exception{
	}

	public RSAKeyPairUtil(boolean bGenKey) throws Exception{
		if(bGenKey) {
			genHexRSAKeyPair();			
		}
	}
	
	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}
	
	/*
	 * 从16进制字符串转换回公钥
	 */
	public static PublicKey getPublicKey(String hexKey) throws Exception {  
        byte[] keyBytes = hexStrToBytes(hexKey);  
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        PublicKey publicKey = keyFactory.generatePublic(keySpec);  
        return publicKey;  
    }  

	/*
	 * 从16进制字符串转换回私钥
	 */
    public static PrivateKey getPrivateKey(String hexKey) throws Exception {  
        byte[] keyBytes =  hexStrToBytes(hexKey);   
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);  
        return privateKey;   
    } 
	

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	

	//获取16进制公钥
	public String getPublicHexKey() {
		return bytesToHexStr(publicKey.getEncoded());
	}

	//获取16进制私钥
	public String getPrivateHexKey() {
		return bytesToHexStr(privateKey.getEncoded());
	}
	
	//设置16进制公钥
	public void setPublicHexKey(String publicHexKey) throws Exception {
		this.publicKey = getPublicKey(publicHexKey);
	}

	//设置16进制私钥
	public void setPrivateHexKey(String privateHexKey) throws Exception {
		this.privateKey = getPrivateKey(privateHexKey);
	}

	
	// 生成密钥对
	public void genHexRSAKeyPair() throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyGen.initialize(512); // 可以理解为：加密后的密文长度，实际原文要小些 越大 加密解密越慢
		KeyPair keyPair = keyGen.generateKeyPair();
		PublicKey localPublicKey = keyPair.getPublic();
		PrivateKey localPrivateKey = keyPair.getPrivate();
		
		//设置公私钥
		setPublicKey(localPublicKey);
		setPrivateKey(localPrivateKey);
	}

	
	
	public static final String bytesToHexStr(byte[] paramArrayOfByte) {
		StringBuffer localStringBuffer = new StringBuffer(paramArrayOfByte.length * 2);
		for (int i = 0; i < paramArrayOfByte.length; i++) {
			localStringBuffer.append(bcdLookup[(paramArrayOfByte[i] >>> 4 & 0xF)]);
			localStringBuffer.append(bcdLookup[(paramArrayOfByte[i] & 0xF)]);
		}
		return localStringBuffer.toString();
	}

	public static final byte[] hexStrToBytes(String paramString) {
		byte[] arrayOfByte = new byte[paramString.length() / 2];
		for (int i = 0; i < arrayOfByte.length; i++)
			arrayOfByte[i] = ((byte) Integer.parseInt(paramString.substring(2 * i, 2 * i + 2), 16));
		return arrayOfByte;
	}
}