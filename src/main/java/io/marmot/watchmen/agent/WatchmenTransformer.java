package io.marmot.watchmen.agent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 *  Transformer
 */
public class WatchmenTransformer implements ClassFileTransformer {



    final static String prefix = "\nlong startTime = System.currentTimeMillis();\n";
    final static String postfix = "\nlong endTime = System.currentTimeMillis();\n";
    /**
     * 密码
     */
    private final static Map<String, List<String>> METHOD_MAP=new HashMap<>();


    public WatchmenTransformer(){

        add("io.marmot.lock.App.test");
        add("io.marmot.lock.App.testHello");

    }

    /**
     * 添加需要监控方法
     * @param methodString
     */
    private void add(String methodString) {
        String className = methodString.substring(0, methodString.lastIndexOf("."));
        String methodName = methodString.substring(methodString.lastIndexOf(".") + 1);
        List<String> list = METHOD_MAP.get(className);
        if (list == null) {
            list = new ArrayList<>();
            METHOD_MAP.put(className, list);
            list.add(methodName);
        }
    }


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("className:"+className);
        className = className.replace("/", ".");

        if(!METHOD_MAP.containsKey(className)){
            System.out.println("exclude className:"+className);
            return null;
        }

        System.out.println("className:"+className);

        CtClass ctclass = null;
        try {
            ctclass = ClassPool.getDefault().get(className);// 使用全称,用于取得字节码类<使用javassist>
            for (String methodName : METHOD_MAP.get(className)) {
                String outputStr = "\nSystem.out.println(\"this method " + methodName
                        + " cost:\" +(endTime - startTime) +\"ms.\");";

                CtMethod ctmethod = ctclass.getDeclaredMethod(methodName);// 得到这方法实例
                String newMethodName = methodName + "$old";// 新定义一个方法叫做比如sayHello$old
                ctmethod.setName(newMethodName);// 将原来的方法名字修改

                // 创建新的方法，复制原来的方法，名字为原来的名字
                CtMethod newMethod = CtNewMethod.copy(ctmethod, methodName, ctclass, null);

                // 构建新的方法体
                StringBuilder bodyStr = new StringBuilder();
                bodyStr.append("{");
                bodyStr.append(prefix);
                bodyStr.append(newMethodName + "($$);\n");// 调用原有代码，类似于method();($$)表示所有的参数
                bodyStr.append(postfix);
                bodyStr.append(outputStr);
                bodyStr.append("}");

                newMethod.setBody(bodyStr.toString());// 替换新方法
                ctclass.addMethod(newMethod);// 增加新方法
            }
            return ctclass.toBytecode();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
