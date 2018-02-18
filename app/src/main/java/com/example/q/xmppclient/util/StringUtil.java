package com.example.q.xmppclient.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by q on 2017/11/7.
 */

public class StringUtil {

    /**
     * 给用户名返回JID
     *
     * @param jidFor
     *            域名//如ahic.com.cn
     * @param userName
     * @return
     */
    public static String getJidByName(String userName, String jidFor) {
        if (empty(userName) || empty(jidFor)) {
            return null;
        }
        return userName + "@" + jidFor;
    }

    /**
     * 给JID返回用户名
     *
     * @param Jid
     * @return
     */
    public static String getUserNameByJid(String Jid) {
        if (empty(Jid)) {
            return null;
        }
        if (!Jid.contains("@")) {
            return Jid;
        }
        return Jid.split("@")[0];
    }
    public static String stringFilter(String str) throws PatternSyntaxException {
        // 只允许字母、数字和汉字      
        String regEx = "[^a-zA-Z0-9\u4E00-\u9FA5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    final static String PLEASE_SELECT = "请选择...";

    public static boolean notEmpty(Object o) {
        return o != null && !"".equals(o.toString().trim())
                && !"null".equalsIgnoreCase(o.toString().trim())
                && !"undefined".equalsIgnoreCase(o.toString().trim())
                && !PLEASE_SELECT.equals(o.toString().trim());
    }

    /**
     * 处理空字符串
     *
     * @param str
     * @return String
     */
    public static String doEmpty(String str) {
        return doEmpty(str, "");
    }

    /**
     * 处理空字符串
     *
     * @param str
     * @param defaultValue
     * @return String
     */
    public static String doEmpty(String str, String defaultValue) {
        if (str == null || str.equalsIgnoreCase("null")
                || str.trim().equals("") || str.trim().equals("－请选择－")) {
            str = defaultValue;
        } else if (str.startsWith("null")) {
            str = str.substring(4, str.length());
        }
        return str.trim();
    }
    public static boolean empty(Object o) {
        return o == null || "".equals(o.toString().trim())
                || "null".equalsIgnoreCase(o.toString().trim())
                || "undefined".equalsIgnoreCase(o.toString().trim())
                || PLEASE_SELECT.equals(o.toString().trim());
    }

}
