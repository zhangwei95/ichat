package com.example.q.xmppclient.util;

import android.widget.TextView;

import org.jivesoftware.smack.util.StringUtils;


/**
 * Created by q on 2017/10/26.
 */

public class ValidataUtil {

    /**
     *  属性信息判断是否为空
     * @param view 文本框类对象
     * @param str 用户名，密码之类的不能字符串对象
     * @return
     */
    public static  boolean isEmpty(TextView view, String str)
    {
        if (view.getText().toString().trim().isEmpty()) {
            view.setError(str + "不能为空！");
            view.setFocusable(true);
            view.requestFocus();
            return true;
        }
        return false;
    }

    /**
     *  密码有效性判断
     * @param view 文本框类对象
     * @return
     */
    public  static boolean validatePwd(TextView view){
        String result="验证通过";
//		if (str.length()<8) {
//			result="密码不能少于8位，请您重新输入";
//		} else if (str.length()>20) {
//		result="密码最大长度为20位，请您重新输入";
//		} else if(str.matches("^[A-Za-z0-9]")) {
//		result="密码不能包含特殊字符，请您重新输入";
//		} else if (str.matches("(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])[a-zA-Z0-9]{6,15}")) {
//		result="密码必须同时包含大小写字母和数字";
//		}
        if(view.getText().toString().trim().matches
                ("(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])[a-zA-Z0-9]{6,15}")){
            return true;
        }else {
            result="密码6-15位同时包含大小写字母和数字且不含特殊字符";
            view.setError(result);
            view.setFocusable(true);
            view.requestFocus();
            return false;
        }

    }
    /**
     *  用户名有效性判断
     * @param view 文本框类对象
     * @return
     */
    public  static boolean validateUsername(TextView view) {
        String result="验证通过";
        if(view.getText().toString().trim().matches
                ("(?=.*[A-Za-z])(?=.*[0-9])[a-zA-Z0-9]{6,15}")){
            if(view.getText().toString().substring(0, 1).matches("[a-zA-Z]")){
                return true;
            }else{
                result="用户名首字符必须为字母";
                view.setError(result);
                view.setFocusable(true);
                view.requestFocus();
                return false;
            }
        }else {
            result="用户名由6-15位字母和数字组成且不含特殊字符";
            view.setError(result);
            view.setFocusable(true);
            view.requestFocus();
            return false;
        }
    }
}
