package com.yzchnb.GeneratorUsage;

import com.yzchnb.OracleFunctionParser;
import com.yzchnb.entity.FunctionDetail;

import static com.yzchnb.Utils.NameTransformer.snakeToPasca;

public class InterfaceGenerator implements Generator{
    private String mapperPackage;
    public InterfaceGenerator(String mapperPackage){
        this.mapperPackage = mapperPackage;
    }

    @Override
    public String generate(FunctionDetail detail) throws Exception{
        String template = "package @mapperPackage@;\n" +
                "\n" +
                "import org.apache.ibatis.annotations.Mapper;\n" +
                "import org.springframework.stereotype.Repository;\n" +
                "\n" +
                "import java.util.Map;\n" +
                "\n" +
                "@Mapper\n" +
                "@Repository\n" +
                "public interface @functionName@Mapper {\n" +
                "\tvoid call(Map map);\n" +
                "}\n";
        String functionName = snakeToPasca(detail.getFunctionName());
        return template
                .replaceAll("@mapperPackage@", mapperPackage)
                .replaceAll("@functionName@", functionName);
    }

    public static void main(String[] args) {
        OracleFunctionParser parser = new OracleFunctionParser(
                "jdbc:oracle:thin:@106.13.82.84:1521:helowin",
                "yzcdba",
                "123456",
                "oracle.jdbc.OracleDriver",
                "/Users/purchaser/IdeaProjects/twitter/ojdbc6-11.2.0.3.jar"
        );
        InterfaceGenerator generator = new InterfaceGenerator(
                "com.yzchnb.twitter.dao.FunctionMapper");
        try{
            parser.connectDB();
            parser.initTableData();
            FunctionDetail detail = parser.testParseFunction();
            System.out.println(detail);
            String interfaceCode = generator.generate(detail);
            System.out.println("java code:");
            System.out.println(interfaceCode);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
