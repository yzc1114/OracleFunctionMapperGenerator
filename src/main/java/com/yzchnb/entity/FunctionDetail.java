package com.yzchnb.entity;

import java.util.ArrayList;
import java.util.HashMap;

public class FunctionDetail {
    private ArrayList<String> tableNames;
    private HashMap<String, HashMap<String, String>> tableName2Fields;
    private HashMap<String, String> fieldsToType;
    private String functionName;
    private String returnType;
    private ArrayList<Param> params = new ArrayList<>();
    private ArrayList<String> refcursorFields;

    public ArrayList<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(ArrayList<String> tableNames) {
        this.tableNames = tableNames;
    }

    public HashMap<String, HashMap<String, String>> getTableName2Fields() {
        return tableName2Fields;
    }

    public void setTableName2Fields(HashMap<String, HashMap<String, String>> tableName2Fields) {
        this.tableName2Fields = tableName2Fields;
    }

    public HashMap<String, String> getFieldsToType() {
        return fieldsToType;
    }

    public void setFieldsToType(HashMap<String, String> fieldsToType) {
        this.fieldsToType = fieldsToType;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public ArrayList<Param> getParams() {
        return params;
    }

    public ArrayList<String> getRefcursorFields() {
        return refcursorFields;
    }

    public void setRefcursorFields(ArrayList<String> refcursorFields) {
        this.refcursorFields = refcursorFields;
    }

    public void setParams(ArrayList<Param> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "FunctionDetail{" +
                "functionName='" + functionName + '\'' +
                ", returnType='" + returnType + '\'' +
                ", params=" + params +
                ", refcursorFields=" + refcursorFields +
                '}';
    }
}
