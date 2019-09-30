package com.yzchnb.Utils;

import java.util.HashMap;

public class TypeTransformer {

    private static HashMap<String, String> jdbcTypeToJavaType = new HashMap<>();
    private static HashMap<String, String> oracleTypeToJdbcType = new HashMap<>();
    static {
        jdbcTypeToJavaType.put("INTEGER", "java.lang.Integer");
        jdbcTypeToJavaType.put("CURSOR", "java.sql.ResultSet");
        jdbcTypeToJavaType.put("VARCHAR", "java.lang.String");
        oracleTypeToJdbcType.put("NUMBER", "INTEGER");
        oracleTypeToJdbcType.put("VARCHAR2", "VARCHAR");
        oracleTypeToJdbcType.put("VARCHAR", "VARCHAR");
        oracleTypeToJdbcType.put("INTEGER", "INTEGER");
        oracleTypeToJdbcType.put("SYS_REFCURSOR", "CURSOR");
    }

    public static String jdbcToJava(String jdbcType) {
        return jdbcTypeToJavaType.get(jdbcType);
    }

    public static String oracleToJdbc(String oracleType) {
        return oracleTypeToJdbcType.get(oracleType);
    }

    public static String oracleToJava(String oracleType) {
        return jdbcTypeToJavaType.get(oracleTypeToJdbcType.get(oracleType));
    }
}
