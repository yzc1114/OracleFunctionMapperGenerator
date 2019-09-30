package com.yzchnb.Utils;

public class NameTransformer {

    public static String snakeToPasca(String param){
        String res = snakeToCamel(param);
        return ((Character)res.charAt(0)).toString().toUpperCase() + res.substring(1);
    }

    public static String snakeToCamel(String param){
        param = param.toLowerCase();
        if ("".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == '_') {
                if (++i < len) {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
