package com.github.bluecatlee.dcep.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DecipherUtil_Ft {
	public final static String TIME_PATTERN = "HHmmss";
	public final static String DATE_PATTERN = "yyyy:MM:dd";

	/**
	 * 解密并验证签名，适用于金融瓦片
	 * 
	 * @param strDecryptedSrc
	 *            加密串
	 * @param strPubKey
	 *            公钥
	 * @param strPrivateHexKey
	 *            私钥
	 * @return String 明文
	 * @throws Exception 
	 */
	public static String decipherWithRSASignandAES(String strDecrypted, String publicHexKey) throws Exception {
		String strDecryptedSrc = AESUtil.decrypt(strDecrypted, publicHexKey.substring(publicHexKey.length() - 32));
		
		String strAssembledParam = strDecryptedSrc.substring(0, strDecryptedSrc.indexOf("&SIGN="));
		String strSign = strDecryptedSrc.substring(strDecryptedSrc.indexOf("&SIGN=") + 6, strDecryptedSrc.length());
		
		boolean sha1Verifty = SHA1withRSADigitalSignatureUtil.verifySHA1withRSASign(strAssembledParam, strSign, publicHexKey);

		if (!sha1Verifty) {
			System.out.println("DecipherUtil_Ft:RSA验签失败");
			return null;
		}

		String checkTimeoutConfig = "1";// Config.getStrPara("FTGATE_CHK_TIMEOUT", "1");
		if ("1".equals(checkTimeoutConfig)) {
			try {
				SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
				
				String genTime = strAssembledParam.substring(strAssembledParam.indexOf("SYS_TIME=") + 9);

						
				Date genDate = sdfDateTime.parse(genTime); // 生成时间
				Date currDate = new Date(); // 当前时间

				System.out.println("currDate:" + currDate +"--genDate:" + genDate);
				
				if ((currDate.getTime() - genDate.getTime()) >= 150*60*1000) { // 超时判断
					System.out.println("DecipherUtil_Ft:请求时间串超时！");
					return null;
				}
			} catch (Exception e) {
				System.out.println("DecipherUtil_Ft:验证请求时间异常：" + e.getMessage());
				return null;
			}
		}
		strAssembledParam=strAssembledParam.substring(0,strAssembledParam.indexOf("&SYS_TIME="));
		return strAssembledParam;
	}

}
