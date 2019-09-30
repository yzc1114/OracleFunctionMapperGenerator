package com.yzchnb;

import com.yzchnb.GeneratorUsage.BaseCallerGenerator;
import com.yzchnb.GeneratorUsage.ImplGenerator;
import com.yzchnb.GeneratorUsage.InterfaceGenerator;
import com.yzchnb.GeneratorUsage.XmlGenerator;
import com.yzchnb.entity.FunctionDetail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import static com.yzchnb.Utils.NameTransformer.snakeToPasca;

public class CodeGenerator {
    private String xmlDir;
    private String mapperPackage;
    private String callerPackage;
    private String projectRootDir;

    public CodeGenerator(String xmlDir, String mapperPackage, String callerPackage, String projectRootDir) {
        this.xmlDir = xmlDir;
        this.mapperPackage = mapperPackage;
        this.callerPackage = callerPackage;
        this.projectRootDir = projectRootDir;
    }

    private OracleFunctionParser parser = null;

    public void setParser(OracleFunctionParser parser){
        this.parser = parser;
    }

    public void generate() throws Exception{
        if(parser == null){
            throw new Exception("parser not initialized");
        }
        generateBaseCaller();
        ArrayList<String> functionNames = parser.getFunctionNames();
        for (int i = 0; i < functionNames.size(); i++) {
            FunctionDetail functionDetail = parser.parseFunction(functionNames.get(i));
            generateCode(functionDetail);
        }
    }

    private void generateCode(FunctionDetail functionDetail) throws Exception{
        String functionNamePasca = snakeToPasca(functionDetail.getFunctionName());
        String callerDir = projectRootDir + File.separator + callerPackage.replaceAll("\\.", File.separator);
        String mapperDir = projectRootDir + File.separator + mapperPackage.replaceAll("\\.", File.separator);
        //xml
        XmlGenerator xmlGenerator = new XmlGenerator(mapperPackage);
        String xml = xmlGenerator.generateMapperXml(functionDetail);
        writeToFile(xmlDir + File.separator + functionNamePasca + "Mapper.xml", xml);
        //mapperInterface
        InterfaceGenerator interfaceGenerator = new InterfaceGenerator(mapperPackage);
        String interfaceCode = interfaceGenerator.generate(functionDetail);
        writeToFile(mapperDir + File.separator + functionNamePasca + "Mapper.java", interfaceCode);
        //mapperImplementation
        ImplGenerator implGenerator = new ImplGenerator(callerPackage, mapperPackage);
        String implCode = implGenerator.generate(functionDetail);
        writeToFile(callerDir + File.separator + functionNamePasca + "Caller.java", implCode);
    }

    private void generateBaseCaller() throws Exception{
        BaseCallerGenerator.callerPackage = callerPackage;
        File baseCallerDir = new File(projectRootDir, callerPackage.replaceAll("\\.", File.separator) + File.separator + "BaseCaller");
        if(!baseCallerDir.exists()){
            if(!baseCallerDir.mkdir()){
                throw new Exception("make baseCallerDir failed");
            }
        }
        String baseCallerCode = BaseCallerGenerator.generate();
        writeToFile(baseCallerDir + File.separator + "FuncBaseCaller.java", baseCallerCode);
    }

    private static void writeToFile(String filePath, String content) throws Exception{
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
        bufferedWriter.write(content);
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public static void main(String[] args) throws Exception {
        OracleFunctionParser parser = new OracleFunctionParser(
                "jdbc:oracle:thin:@106.13.82.84:1521:helowin",
                "yzcdba",
                "123456",
                "oracle.jdbc.OracleDriver",
                "/Users/purchaser/IdeaProjects/twitter/ojdbc6-11.2.0.3.jar"
        );
        parser.connectDB();
        parser.initTableData();
        FunctionDetail functionDetail = parser.testParseFunction();
        String projectRootDir = "/src/main/java";
        String callerPackage = "com.yzchnb.twitter.dao.FunctionCaller";
        String mapperPackage = "com.yzchnb.twitter.dao.FunctionMapper";
        System.out.println(functionDetail);
        String functionNamePasca = snakeToPasca(functionDetail.getFunctionName());
        String callerDir = projectRootDir + File.separator + callerPackage.replaceAll("\\.", File.separator);
        String mapperDir = projectRootDir + File.separator + mapperPackage.replaceAll("\\.", File.separator);
        //xml
        XmlGenerator xmlGenerator = new XmlGenerator(mapperPackage);
        String xml = xmlGenerator.generateMapperXml(functionDetail);
        System.out.println("xml:");
        System.out.println(xml);
        //mapperInterface
        InterfaceGenerator interfaceGenerator = new InterfaceGenerator(mapperPackage);
        String interfaceCode = interfaceGenerator.generate(functionDetail);
        System.out.println("interfaceCode:");
        System.out.println(interfaceCode);
        //mapperImplementation
        ImplGenerator implGenerator = new ImplGenerator(callerPackage, mapperPackage);
        String implCode = implGenerator.generate(functionDetail);
        System.out.println("implCode:");
        System.out.println(implCode);
    }

}
