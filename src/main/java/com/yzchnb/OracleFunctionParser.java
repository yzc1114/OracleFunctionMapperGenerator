package com.yzchnb;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.yzchnb.entity.FunctionDetail;
import com.yzchnb.entity.Param;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OracleFunctionParser
{
    private String jdbc_url;
    private String jdbc_username;
    private String jdbc_password;
    private String jdbc_driver_class_name;
    private String jdbc_driver_jar_path;

    public OracleFunctionParser(String jdbc_url, String jdbc_username, String jdbc_password, String jdbc_driver_class_name, String jdbc_driver_jar_path) {
        this.jdbc_url = jdbc_url;
        this.jdbc_username = jdbc_username;
        this.jdbc_password = jdbc_password;
        this.jdbc_driver_class_name = jdbc_driver_class_name;
        this.jdbc_driver_jar_path = jdbc_driver_jar_path;
    }

    private static String[] reservedWordsAfterFrom = {"group", "minus", "order", "union", "where"};

    private ArrayList<String> tableNames = null;
    private HashMap<String, HashMap<String, String>> tableName2Fields = new HashMap<>();
    private HashMap<String, String> fieldsToType = new HashMap<>();

    public void setJdbc_url(String jdbc_url) {
        this.jdbc_url = jdbc_url;
    }


    public void setJdbc_username(String jdbc_username) {
        this.jdbc_username = jdbc_username;
    }

    public void setJdbc_password(String jdbc_password) {
        this.jdbc_password = jdbc_password;
    }

    public void setJdbc_driver_jar_path(String jdbc_driver_jar_path) {
        this.jdbc_driver_jar_path = jdbc_driver_jar_path;
    }

    private Connection conn;

    public void connectDB() throws Exception {
        File jarFile = new File(jdbc_driver_jar_path); // 从URLClassLoader类中获取类所在文件夹的方法，jar也可以认为是一个文件夹

        if (!jarFile.exists()) {
            System.out.println("jar file not found.");
            return;
        }

        URLClassLoader loader = new URLClassLoader(new URL[]{ jarFile.toURI().toURL() });
        Class clazz = loader.loadClass(jdbc_driver_class_name);
        Driver driver = (Driver) clazz.newInstance();
        Properties p = new Properties();
        p.put("user", jdbc_username);
        p.put("password", jdbc_password);
        Connection con = driver.connect(jdbc_url, p);
        this.conn = con;
        System.out.println(con);
    }


    public ArrayList<String> getFunctionNames() throws Exception{
        String sql = "select OBJECT_NAME from USER_OBJECTS where object_type = 'FUNCTION'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        ArrayList<String> functions = new ArrayList<String>();
        while(rs.next()){
            functions.add(rs.getString(1));
        }
        return functions;
    }

    private String getFunctionContent(String functionName) throws Exception{
        StringBuilder builder = new StringBuilder();
        String sql = "select text from user_source where name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, functionName);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            builder.append(rs.getString(1));
        }
        return builder.toString();
    }

    public FunctionDetail parseFunction(String functionName) throws Exception{
        return parseFunctionContent(getFunctionContent(functionName));
    }



    public static void main(String[] args) {
        OracleFunctionParser mojo = new OracleFunctionParser(
                "jdbc:oracle:thin:@106.13.82.84:1521:helowin",
                "yzcdba",
                "123456",
                "oracle.jdbc.OracleDriver",
                "/Users/purchaser/IdeaProjects/twitter/ojdbc6-11.2.0.3.jar"
        );
//        mojo.test();
//        mojo.testParseFunction();
        try{
            mojo.connectDB();
            mojo.initTableData();
            mojo.testParseFunction();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initTableData() throws Exception{
        String sql = "select TABlE_NAME from user_tables";
        PreparedStatement stmtForTableNames = conn.prepareStatement(sql);
        ResultSet rs = stmtForTableNames.executeQuery();
        ArrayList<String> tableNames = new ArrayList<String>();
        while(rs.next()){
            tableNames.add(rs.getString(1).toUpperCase());
        }
        this.tableNames = tableNames;
        System.out.println("tableNames: " + tableNames);
        sql = "select COLUMN_NAME, DATA_TYPE  from user_tab_columns where TABLE_NAME = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (String tableName: tableNames) {
            stmt.setString(1, tableName);
            ResultSet rsOfTableContent = stmt.executeQuery();
            HashMap<String, String> tableFields = new HashMap<String, String>();
            while(rsOfTableContent.next()){
                String columnName = rsOfTableContent.getString(1).toUpperCase();
                String oracleDataType = rsOfTableContent.getString(2).toUpperCase();
                tableFields.put(columnName, oracleDataType);
            }
            System.out.println("querying " + tableName + " fields: " +tableFields);
            tableName2Fields.put(tableName, tableFields);
        }
        for (int i = 0; i < this.tableNames.size(); i++) {
            String tableName = this.tableNames.get(i);
            HashMap<String, String> fields = tableName2Fields.get(tableName);
            for (String colName : fields.keySet()) {
                fieldsToType.put(colName, fields.get(colName));
            }
        }
    }

    public FunctionDetail testParseFunction() {
        try{
            String functionName = "func_if_following".toUpperCase();
            FileReader reader = new FileReader("/Users/purchaser/Desktop/oracleFunctions/" + functionName);
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[10];
            int c = reader.read(buffer);
            while(c != -1){
                builder.append(buffer, 0, c);
                c = reader.read(buffer);
            }
            String content = builder.toString();
            return parseFunctionContent(content);

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void test(){
        setJdbc_driver_jar_path("/Users/purchaser/IdeaProjects/twitter/ojdbc6-11.2.0.3.jar");
        setJdbc_password("123456");
        setJdbc_url("jdbc:oracle:thin:@106.13.82.84:1521:helowin");
        setJdbc_username("yzcdba");
        try{
            connectDB();
            ArrayList<String> functionNames = getFunctionNames();
            for (int i = 0; i < functionNames.size(); i++) {
                System.out.print("Get function: " + functionNames.get(i) + ", content:");
                String functionContent = getFunctionContent(functionNames.get(i));
                System.out.println(functionContent.replaceAll("\n", " ").replaceAll(" +", " "));
                String fileName = "/Users/purchaser/Desktop/oracleFunctions/" + functionNames.get(i);
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                writer.write(functionContent);
                writer.flush();
                writer.close();
            }
            conn.close();
        }catch (Exception e){
            System.out.println("error occurred");
            e.printStackTrace();
        }
    }

    public FunctionDetail parseFunctionContent(String content) throws Exception{
        FunctionDetail functionDetail = new FunctionDetail();
        functionDetail.setTableNames(tableNames);
        functionDetail.setFieldsToType(fieldsToType);
        functionDetail.setTableName2Fields(tableName2Fields);

        System.out.println(content);
        //step1: flatten the code and add space for special symbols
        content = content.toLowerCase();
        content = content.replaceAll("\n", " ").trim().replaceAll(" +", " ");
        //return the Attributes of the selection sql;
        content = addSpaceAroundSpecialCharater(content);
        System.out.print("after correction. content:");
        System.out.println(content);
        String functionHeader = content.split("begin")[0];
        String functionBody = content.split("begin")[1].split("end;")[0];

        //step2: parse the function header

        if(!content.toLowerCase().startsWith("function")){
            throw new Exception("not a function");
        }
        String regexFunctionSignaturePattern = "function (\\w*) *\\(([ \\w,]*)\\).*";
        Pattern functionSignaturePattern = Pattern.compile(regexFunctionSignaturePattern);

        Matcher matcherForFunctionSignature = functionSignaturePattern.matcher(functionHeader);
        if(!matcherForFunctionSignature.find()){
            throw new Exception("function signature match failed");
        }

        //TODO: add to an object
        String functionName = matcherForFunctionSignature.group(1);
        functionDetail.setFunctionName(functionName);
        System.out.println("functionName: " + functionName);

        String functionParamString = matcherForFunctionSignature.group(2);

        String[] functionParams = functionParamString.split(",");
        String sys_refcursor = null;
        for (String param: functionParams) {
            String[] attrs = param.trim().replaceAll(" +", " ").split(" ");
            if(attrs.length != 3){
                throw new Exception("param attrs length != 3");
            }
            //TODO: add them to an object
            String paramName = attrs[0]; String paramDirection = attrs[1]; String paramOracleType = attrs[2];
            Param paramEntity = new Param();
            paramEntity.setParamName(paramName); paramEntity.setParamDirection(paramDirection); paramEntity.setParamType(paramOracleType);
            functionDetail.getParams().add(paramEntity);
            if(paramOracleType.equals("sys_refcursor")){
                sys_refcursor = paramName;
            }
        }

        String regexReturnType = ".* return (\\w+) .*";
        Pattern returnTypePattern = Pattern.compile(regexReturnType);
        Matcher matcherForReturnType = returnTypePattern.matcher(functionHeader);
        if(!matcherForReturnType.find()){
            throw new Exception("return type match failed");
        }

        String returnType = matcherForReturnType.group(1);
        functionDetail.setReturnType(returnType);
        System.out.println("function return type: " + returnType);

        if(sys_refcursor == null){
            //In this case, this function has no return result set;
            //we can skip everything for now;
            return functionDetail;
        }


        //step3: parse function body
        System.out.println("cursorName:" + sys_refcursor);

        String openCursorRegex = ".*open " + sys_refcursor + " for.*";
        String openCursor = "open " + sys_refcursor + " for";
        if(!functionBody.matches(openCursorRegex)){
            throw new Exception("function which has refcursor doesn't contain 'open xxx for'");
        }
        String selectSQL = functionBody.split(openCursor)[1].split(";")[0].trim().replaceAll(" +", " ");
        ArrayList<String> fields = this.recursiveAnalyseFields(selectSQL);
        System.out.println("function ref_cursor return fields :" + fields);
        functionDetail.setRefcursorFields(fields);
        return functionDetail;
    }

    private ArrayList<String> recursiveAnalyseFields(String selectSQL){
        //return the Attributes of the selection sql;
        selectSQL = addSpaceAroundSpecialCharater(selectSQL);

        selectSQL = selectSQL.trim().replaceAll(" +", " ");

        String selectAnyFromAny = "\\(? *select(.*?)from(.*)$";
        Pattern patternForSelectAnyFromAny = Pattern.compile(selectAnyFromAny);
        Matcher matcherForSelectAnyFromAny = patternForSelectAnyFromAny.matcher(selectSQL);
        if(matcherForSelectAnyFromAny.find() && (selectSQL.startsWith("( select") || selectSQL.startsWith("select"))){
            //query must have left bracket
            //also we must use regex to ensure there is a select afterwards
            String betweenSelectAndFrom = matcherForSelectAnyFromAny.group(1).trim().replaceAll(" +", " ");
            System.out.println("betweenSelectAndFrom is : " + betweenSelectAndFrom);
            if(betweenSelectAndFrom.equals("*")){
                //return all the sub-query from stuff;
                return recursiveAnalyseFields(matcherForSelectAnyFromAny.group(2));
            }
            //when its not *, when don't need to go on analyse
            //now string should be "A.col1 as, B.col2 colNameAlias , col3 from .*"
            System.out.println("got str between select and from : " + betweenSelectAndFrom + " when analysing :" + selectSQL);
            return parseBetweenSelectAndFrom(betweenSelectAndFrom);
        }else{
            //fields
            //This string could have other use less shit
            //just ignore those
            System.out.println("before split: fields = " + selectSQL);
            String fromContent = selectSQL.trim().replaceAll(" +", " ");
            for (int i = 0; i < reservedWordsAfterFrom.length; i++) {
                String fieldsOption = selectSQL.split(reservedWordsAfterFrom[i])[0].trim().replace(" +", " ");
                if(fieldsOption.length() < fromContent.length()){
                    fromContent = fieldsOption;
                }
            }
            System.out.println("got fields : " + fromContent + ", when analysing selectSQL : " + selectSQL);
            ArrayList<String> tableNames = parseFromContent(fromContent);
            ArrayList<String> attrs = new ArrayList<>();
            for (String tableName : tableNames) {
                HashMap<String, String> fields = tableName2Fields.get(tableName);
                for (String s : fields.keySet()) {
                    if(!attrs.contains(s)){
                        attrs.add(s);
                    }
                }
            }
            return attrs;
        }
    }

    private static String addSpaceAroundSpecialCharater(String origin){
        //return the Attributes of the selection sql;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < origin.length(); i++) {
            if(Character.isJavaIdentifierPart(origin.charAt(i))
            || Character.isSpaceChar(origin.charAt(i))){
                builder.append(origin.charAt(i));
            }else{
                builder.append(" ");
                builder.append(origin.charAt(i));
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    private ArrayList<String> parseFromContent(String content) {
        //FromContent could be like "table1 , table2 as t2 , table2 t2 , table1 natural left outer join table2
        String[] symbols = content.replaceAll(" +", " ").split(" ");
        ArrayList<String> tableNames = new ArrayList<>();
        for (String symbol: symbols) {
            if(this.tableNames.contains(symbol.toUpperCase())){
                tableNames.add(symbol.toUpperCase());
            }
        }
        return tableNames;
    }

    private ArrayList<String> parseBetweenSelectAndFrom(String content) {
        //content could be like "A . col1 , B . col2 as alias , C . col4 alias_too , user_id , user_id as aa, user_id a"
        String[] fields = content.split(",");
        ArrayList<String> attrs = new ArrayList<>();
        for (String field: fields) {
            field = field.trim().replaceAll(" +", " ");
            String[] splitDot = field.split("\\.");
            field = splitDot[splitDot.length - 1];
            field = field.trim().replaceAll(" +", " ").split(" ")[0];
            attrs.add(field.toUpperCase());
        }
        return attrs;
    }
}
