package util;

import com.sun.net.httpserver.Authenticator.Success;

/**
 * 登录或注册可能抛出的异常
 * @author 马啸远
 *
 */
public enum SignInfo {
	SUCCESS("Success!"),
	WRONG_PASSWORD("Wrong password!"),
	USER_NOT_EXIST("User dosen't exist!"),
	USER_HAS_EXIST("User has been existed!"),
	INVALID_VALUE("Invalid name or password!");
	
	private String message;
	private SignInfo(String msg) {
		message = msg;
	}
	
	public static void main(String[] args) {
		SignInfo info = SignInfo.INVALID_VALUE;
		System.out.println(info);
	}
}





