package com.github.bluecatlee.dcep.utils;

import java.text.SimpleDateFormat;
import java.util.Date;


public class EncipherUtil_Ft {

	/**
	 * 加密字符串
	 * 
	 * @param FT_CORPID
	 *            系统编号
	 * @param vName
	 *            原串变量
	 * @param vValue
	 *            原串参数
	 * @param encryptKey
	 *            密钥
	 * @return String 密文串
	 */
	public final static String TIME_PATTERN = "HHmmss";
	public final static String DATE_PATTERN = "";

	/**
	 * 加密字符串，适用于金融瓦片
	 * 
	 * @param strSrc
	 *            源串
	 * @param FT_CORPID
	 *            系统编号
	 * @param encryptKey
	 *            密钥
	 * @return String 加密串
	 * @throws Exception 
	 * @since 20190918
	 */
	public static String encipherWithRSASignandAES(String strSrc, String publicHexKey, String privateHexKey) throws Exception {
		SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String strDateTime = sdfDateTime.format(new Date());

		System.out.println("===>EncipherUtil_Ft.encipherWithRSASignandAES输入参数strSrc:"+strSrc);
		System.out.println("===>EncipherUtil_Ft.encipherWithRSASignandAES输入参数publicHexKey:"+publicHexKey);
		System.out.println("===>EncipherUtil_Ft.encipherWithRSASignandAES输入参数privateHexKey:"+privateHexKey);
		String strAssembled = strSrc + "&SYS_TIME=" + strDateTime;

		System.out.println("===>EncipherUtil_Ft.encipherWithRSASignandAES参数strAssembled:"+strAssembled);
		String strSign = SHA1withRSADigitalSignatureUtil.genSha1Sign(strAssembled, privateHexKey);
		System.out.println("===>EncipherUtil_Ft.encipherWithRSASignandAES参数strSign:"+strSign);
	
		strAssembled = strAssembled + "&SIGN=" + strSign;
		System.out.println("===>EncipherUtil_Ft.encipherWithRSASignandAES参数strAssembled:"+strAssembled);
		
		String encipheredResult = AESUtil.encrypt(strAssembled, publicHexKey.substring(publicHexKey.length() - 32));
		System.out.println("===>EncipherUtil_Ft.encipherWithRSASignandAES参数encipheredResult:"+encipheredResult);
		
		return encipheredResult;
	}

}
