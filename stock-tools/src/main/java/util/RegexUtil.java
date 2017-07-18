package util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

	public static String getFirstFromContent(String content, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		String result = null;
		while (m.find()) {
			result = m.group();
			break;
		}
		return result;
	}

	public static List<String> getAllFromContent(String content, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		List<String> result = new ArrayList<String>();
		while (m.find()) {
			result.add(m.group());
		}
		return result;
	}

}
