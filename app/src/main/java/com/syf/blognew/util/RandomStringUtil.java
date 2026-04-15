package com.syf.blognew.util;

import java.util.Random;

public class RandomStringUtil {

	private static Random random = new Random();
	private static char[] numbers = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
	// 移除 O o 0 i 1 有歧义的字符
	private static char[] charAndNumbers = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9' };

	public static String getNumber(int size) {
		char[] tmp = new char[size];
		for (int i = 0; i < size; i++) {
			tmp[i] = numbers[random.nextInt(10)];
		}
		return new String(tmp);
	}

	public static String randomString(int size) {
		char[] tmp = new char[size];
		for (int i = 0; i < size; i++) {
			tmp[i] = charAndNumbers[random.nextInt(53)];
		}
		return new String(tmp);
	}

}
