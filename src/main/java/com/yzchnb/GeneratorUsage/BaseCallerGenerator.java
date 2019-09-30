package com.yzchnb.GeneratorUsage;

import com.yzchnb.entity.FunctionDetail;

public class BaseCallerGenerator {
    public static String callerPackage;

    public static String generate() throws Exception {
        return "package " + callerPackage + ".BaseCaller;\n" +
                "\n" +
                "import com.yzchnb.twitter.configs.ExceptionDefinition.InternalException;\n" +
                "import com.yzchnb.twitter.configs.ExceptionDefinition.UserException;\n" +
                "import org.springframework.stereotype.Repository;\n" +
                "\n" +
                "import java.lang.reflect.InvocationTargetException;\n" +
                "import java.lang.reflect.Method;\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "@Repository\n" +
                "public abstract class FuncBaseCaller {\n" +
                "    protected int resolve(Class cls, Object mapper, Map param, ArrayList res) {\n" +
                "        resolve(cls, mapper, param);\n" +
                "        res.clear();\n" +
                "        List temp = (List) param.get(\"data\");\n" +
                "        res.addAll(temp);\n" +
                "        return (int)param.get(\"return\");\n" +
                "    }\n" +
                "\n" +
                "    protected int resolve(Class cls, Object mapper, Map param){\n" +
                "        try{\n" +
                "            Method[] ms = cls.getMethods();\n" +
                "            for (int i = 0; i < ms.length; i++) {\n" +
                "                System.out.println(ms[i].getName() + \" \");\n" +
                "            }\n" +
                "\n" +
                "            ms[0].invoke(mapper, param);\n" +
                "        }catch (IllegalAccessException | InvocationTargetException e){\n" +
                "            System.out.println(\"mapper called failed... This should never happen.\");\n" +
                "            e.printStackTrace();\n" +
                "            throw new InternalException(\"mapper cannot find method named call\");\n" +
                "        }catch (Exception e){\n" +
                "            System.out.println(\"SQL execution failed...\");\n" +
                "            e.printStackTrace();\n" +
                "            throwUserException();\n" +
                "        }\n" +
                "        return (int)param.get(\"return\");\n" +
                "    }\n" +
                "\n" +
                "    private void throwUserException(){\n" +
                "        throw new UserException(\"function call failed: \" + this.getClass().getName());\n" +
                "    }\n" +
                "}\n";
    }
}
