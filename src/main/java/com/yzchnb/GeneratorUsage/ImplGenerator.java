package com.yzchnb.GeneratorUsage;

import com.yzchnb.OracleFunctionParser;
import com.yzchnb.entity.FunctionDetail;
import com.yzchnb.entity.Param;

import java.util.ArrayList;

import static com.yzchnb.Utils.NameTransformer.snakeToCamel;
import static com.yzchnb.Utils.NameTransformer.snakeToPasca;
import static com.yzchnb.Utils.TypeTransformer.oracleToJava;

public class ImplGenerator implements Generator{
    private String callerPackage;
    private String mapperPackage;

    public ImplGenerator(String callerPackage, String mapperPackage) {
        this.callerPackage = callerPackage;
        this.mapperPackage = mapperPackage;
    }

    @Override
    public String generate(FunctionDetail detail) throws Exception{
        String functionNamePasca = snakeToPasca(detail.getFunctionName());
        String functionNameCamel = snakeToCamel(detail.getFunctionName());
        boolean hasRefCursor = false;
        ArrayList<Param> params = detail.getParams();
        for (int i = 0; i < params.size(); i++) {
            if(params.get(i).getParamType().toUpperCase().equals("SYS_REFCURSOR")){
                hasRefCursor = true;
            }
        }
        StringBuilder builder = new StringBuilder("package " + callerPackage + ";\n" +
                "\n" +
                "import " + callerPackage + ".BaseCaller.FuncBaseCaller;\n" +
                "import " + mapperPackage + ".@functionNamePasca@Mapper;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.stereotype.Repository;\n" +
                "\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.HashMap;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "@Repository\n" +
                "public class @functionNamePasca@Caller extends FuncBaseCaller {\n" +
                "\t@Autowired\n" +
                "\tprivate @functionNamePasca@Mapper @functionNameCamel@Mapper;\n" +
                "\n" +
                "\tpublic int call(");
        buildParams(builder, detail);
        builder.append("){\n" +
                "\t\tMap map = new HashMap();\n");
        buildMapPut(builder, detail);
        if(hasRefCursor){
            builder.append("\t\treturn resolve(@functionNamePasca@Mapper.class, @functionNameCamel@Mapper, map, result);\n");
        }else{
            builder.append("\t\treturn resolve(@functionNamePasca@Mapper.class, @functionNameCamel@Mapper, map);\n");
        }
        builder.append(
                "\t}\n" +
                "}\n");
        return builder.toString()
                .replaceAll("@functionNamePasca@", functionNamePasca)
                .replaceAll("@functionNameCamel@", functionNameCamel);
    }

    private void buildMapPut(StringBuilder builder, FunctionDetail detail){
        String template = "\t\tmap.put(\"@paramSnake@\", @paramCamel@);\n";
        ArrayList<Param> params = detail.getParams();
        if(params.size() == 0){
            return;
        }
        for (Param param : params) {
            if(!param.getParamType().toUpperCase().equals("SYS_REFCURSOR")) {
                String paramSnake = param.getParamName();
                String paramCamel = snakeToCamel(paramSnake);
                builder.append(template
                        .replaceAll("@paramSnake@", paramSnake)
                        .replaceAll("@paramCamel@", paramCamel));
            }
        }
    }

    private void buildParams(StringBuilder builder, FunctionDetail detail){
        ArrayList<Param> params = detail.getParams();
        if(params.size() == 0){
            return;
        }
        for (Param param : params) {
            if(param.getParamType().toUpperCase().equals("SYS_REFCURSOR")) {
                builder.append("ArrayList result, ");
            }else{
                builder.append(oracleToJava(param.getParamType().toUpperCase()));
                builder.append(' ');
                builder.append(snakeToCamel(param.getParamName()));
                builder.append(", ");
            }
        }
        builder.delete(builder.length() - 2, builder.length());
    }

    public static void main(String[] args) {
        OracleFunctionParser parser = new OracleFunctionParser(
                "jdbc:oracle:thin:@106.13.82.84:1521:helowin",
                "yzcdba",
                "123456",
                "oracle.jdbc.OracleDriver",
                "/Users/purchaser/IdeaProjects/twitter/ojdbc6-11.2.0.3.jar"
        );
        ImplGenerator generator = new ImplGenerator(
                "com.yzchnb.twitter.dao.FunctionCaller",
                "com.yzchnb.twitter.dao.FunctionMapper");
        try{
            parser.connectDB();
            parser.initTableData();
            FunctionDetail detail = parser.testParseFunction();
            System.out.println(detail);
            String xml = generator.generate(detail);
            System.out.println("java code:");
            System.out.println(xml);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
