package com.yzchnb.GeneratorUsage;

import com.yzchnb.CodeGenerator;
import com.yzchnb.OracleFunctionParser;
import com.yzchnb.Utils.TypeTransformer.*;
import com.yzchnb.entity.FunctionDetail;
import com.yzchnb.entity.Param;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;

import static com.yzchnb.Utils.NameTransformer.snakeToCamel;
import static com.yzchnb.Utils.NameTransformer.snakeToPasca;
import static com.yzchnb.Utils.TypeTransformer.oracleToJava;
import static com.yzchnb.Utils.TypeTransformer.oracleToJdbc;
import static com.yzchnb.Utils.TypeTransformer.jdbcToJava;

public class XmlGenerator implements Generator{
    private String mapperPackage;

    public XmlGenerator(String mapperPackage) {
        this.mapperPackage = mapperPackage;
    }

    public String generateMapperXml(FunctionDetail functionDetail) throws Exception{
        StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                        "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                        "<mapper namespace=\"@packageName@.@mapperName@\">\n" +
                        "\n" +
                        "\t<parameterMap id=\"paramMapX\" type=\"java.util.Map\">\n");
        buildParameters(builder, functionDetail);
        builder.append("\t</parameterMap>\n" + "\n");
        buildResultMap(builder, functionDetail);
        builder.append("\n" +
                        "\t<select id=\"call\" statementType=\"CALLABLE\" parameterMap=\"paramMapX\">\n" +
                        "\t\t{? = call @functionName@(@questionMarks@) }\n" +
                        "\t</select>\n" +
                        "\n" +
                        "</mapper>");
        String xml = builder.toString();
        StringBuilder buildQuestionMarks = new StringBuilder();
        for (int i = 0; i < functionDetail.getParams().size(); i++) {
            buildQuestionMarks.append("?,");
        }
        buildQuestionMarks.deleteCharAt(buildQuestionMarks.length() - 1);
        return xml.replaceAll("@packageName@", mapperPackage)
                .replaceAll("@mapperName@", snakeToPasca(functionDetail.getFunctionName()))
                .replaceAll("@functionName@", functionDetail.getFunctionName().toUpperCase())
                .replaceAll("@questionMarks@", buildQuestionMarks.toString());
    }

    private static void buildParameters(StringBuilder builder, FunctionDetail functionDetail){
        String returnType = functionDetail.getReturnType();
        ArrayList<Param> oracleParams = functionDetail.getParams();
        String template = "\t\t<parameter property=\"@paramName@\"  javaType=\"@javaType@\" jdbcType=\"@jdbcType@\" mode=\"@direction@\"/>\n";
        builder.append(template
                .replaceAll("@paramName@", "return")
                .replaceAll("@javaType@", oracleToJava(returnType.toUpperCase()))
                .replaceAll("@direction@", "OUT")
                .replaceAll("@jdbcType@", oracleToJdbc(returnType.toUpperCase())));
        for (Param oracleParam : oracleParams) {
            builder.append(template
            .replaceAll("@paramName@", oracleParam.getParamType().toUpperCase().equals("SYS_REFCURSOR") ? "data" : snakeToCamel(oracleParam.getParamName().toUpperCase()))
            .replaceAll("@javaType@", oracleToJava(oracleParam.getParamType().toUpperCase()))
            .replaceAll("@jdbcType@", oracleToJdbc(oracleParam.getParamType().toUpperCase()))
            .replaceAll("@direction@", oracleParam.getParamDirection().toUpperCase()));
        }
    }

    private static void buildResultMap(StringBuilder builder, FunctionDetail functionDetail) {
        if(functionDetail.getRefcursorFields() == null || functionDetail.getRefcursorFields().size() == 0){
            return;
        }
        String header = "\t<resultMap id=\"cursorMapX\" type=\"java.util.Map\">\n";
        String template = "\t\t<result property=\"@javaVariableName@\" column=\"@fieldNameInDB@\" jdbcType=\"@jdbcType@\" javaType=\"@javaType@\"/>\n";
        String tailer = "\t</resultMap>\n";
        builder.append(header);
        ArrayList<String> fields = functionDetail.getRefcursorFields();
        for (String field : fields) {
            String javaVariableName = snakeToCamel(field);
            String fieldNameInDB = field.toUpperCase();
            String oracleType = functionDetail.getFieldsToType().get(field.toUpperCase());
            String jdbcType = oracleToJdbc(oracleType);
            String javaType = jdbcToJava(jdbcType);
            builder.append(template
                    .replaceAll("@javaVariableName@", javaVariableName)
                    .replaceAll("@fieldNameInDB@", fieldNameInDB)
                    .replaceAll("@jdbcType@", jdbcType)
                    .replaceAll("@javaType@", javaType));
        }
        builder.append(tailer);
    }

    public static void main(String[] args) {
        OracleFunctionParser parser = new OracleFunctionParser(
                "jdbc:oracle:thin:@106.13.82.84:1521:helowin",
                "yzcdba",
                "123456",
                "oracle.jdbc.OracleDriver",
                "/Users/purchaser/IdeaProjects/twitter/ojdbc6-11.2.0.3.jar"
        );
        XmlGenerator generator = new XmlGenerator("com.yzchnb.twitter.dao.FunctionMapper");
        try{
            parser.connectDB();
            parser.initTableData();
            FunctionDetail detail = parser.testParseFunction();
            System.out.println(detail);
            String xml = generator.generateMapperXml(detail);
            System.out.println("xml:");
            System.out.println(xml);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String generate(FunctionDetail detail) throws Exception {
        return generateMapperXml(detail);
    }
}
