package com.yzchnb;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.regex.Matcher;

@Mojo(name = "generate")
public class Main extends AbstractMojo {

    //项目根目录
    @Parameter(required = true, readonly = true)
    private String projecRootDir;
    //生成mapper.xml路径
    @Parameter(required = true, readonly = true)
    private String xmlDir;
    @Parameter(required = true, readonly = true)
    private String mapperPackage;
    @Parameter(required = true, readonly = true)
    private String callerPackage;
    @Parameter(required = true, readonly = true)
    private String jdbc_url;
    @Parameter(required = true, readonly = true)
    private String jdbc_username;
    @Parameter(required = true, readonly = true)
    private String jdbc_password;
    @Parameter(required = true, readonly = true)
    private String jdbc_driver_class_name = "oracle.jdbc.OracleDriver";
    @Parameter(required = true, readonly = true)
    private String jdbc_driver_jar_path;

    @Override
    public void execute()
            throws MojoExecutionException
    {

        try{
            checkParams();
            OracleFunctionParser oracleFunctionParser = new OracleFunctionParser(
                    jdbc_url, jdbc_username, jdbc_password, jdbc_driver_class_name, jdbc_driver_jar_path
            );
            oracleFunctionParser.connectDB();
            oracleFunctionParser.initTableData();
            CodeGenerator codeGenerator = new CodeGenerator(xmlDir, mapperPackage, callerPackage, projecRootDir);
            codeGenerator.setParser(oracleFunctionParser);
            codeGenerator.generate();
        }catch (Exception e){
            throw new MojoExecutionException("error occurred\n" + e.getMessage());
        }
//        String mapperPath = "";
//        File f = new File(mapperPath);
//
//        if ( !f.exists() )
//        {
//            f.mkdirs();
//        }
//
//        File touch = new File( f, "touch.txt" );
//
//        FileWriter w = null;
//        try
//        {
//            w = new FileWriter( touch );
//
//            w.write( "touch.txt" );
//        }
//        catch ( IOException e )
//        {
//            throw new MojoExecutionException( "Error creating file " + touch, e );
//        }
//        finally
//        {
//            if ( w != null )
//            {
//                try
//                {
//                    w.close();
//                }
//                catch ( IOException e )
//                {
//                    // ignore
//                }
//            }
//        }
    }


    private void checkParams() throws Exception{
        if(projecRootDir == null
                || xmlDir == null
                || mapperPackage == null
                || callerPackage == null
                || jdbc_url == null
                || jdbc_username == null
                || jdbc_password == null
                || jdbc_driver_class_name == null
                || jdbc_driver_jar_path == null){
            throw new Exception("configuration not fulfilled");
        }
        File xmlDir, mapperDir, callerDir;
        xmlDir = new File(this.xmlDir);
        mapperDir = new File(projecRootDir, mapperPackage.replaceAll("\\.", Matcher.quoteReplacement(File.separator)));
        callerDir = new File(projecRootDir, callerPackage.replaceAll("\\.", Matcher.quoteReplacement(File.separator)));
        System.out.println("mapperDir : " + mapperDir);
        System.out.println("callerDir : " + callerDir);
        System.out.println("xmlDir : " + mapperDir);
        if(!xmlDir.exists() || !mapperDir.exists() || !callerDir.exists()){
            throw new Exception("some paths don't exist");
        }
    }
}
