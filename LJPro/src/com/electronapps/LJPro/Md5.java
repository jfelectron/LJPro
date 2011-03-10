package com.electronapps.LJPro;

import java.io.UnsupportedEncodingException; 
import java.math.BigInteger;
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 
 
public class Md5 { 
 
	public static String MD5(String pass)  {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] data = pass.getBytes(); 
		m.update(data,0,data.length);
		BigInteger i = new BigInteger(1,m.digest());
		return String.format("%1$032X", i).toLowerCase();
	}
	
		
		
    


}