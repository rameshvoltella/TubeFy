package com.ramzmania.tubefy.core.extractor.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils 
{
	public static String matchGroup(String pattern, String input) {
        Pattern pat = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher mat = pat.matcher(input);
        if (mat.find()) {
            return mat.group();
        } else
            return null;

	}
	public static List<String> getAllMatches(String pattern, String input) {
        Pattern pat = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher mat = pat.matcher(input);
		List<String> list=new ArrayList<>();
        while(mat.find()) {
            list.add( mat.group());
        } 
		return list;
	}
	
	
	public static boolean hasMatch(String pattern, String input) {
        Pattern pat = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher mat = pat.matcher(input);


        if (mat.find()) {
            return true;
        } else
            return false;

	}
	
	
	public static String getSignatureFromUrl(String url){
		String regexGetSig="(?<=&(sig|s|signature)=).*?([^&]{1,})";

		return RegexUtils.matchGroup(regexGetSig,url);
	}
	
	
	public static String putDechipheredPart(String url,String signature){
		String regexGetSig="(?<=&(sig|s|signature)=).*?([^&]{1,})";

		return url.replaceFirst(regexGetSig,"signature="+signature);
	}
	
	
}
