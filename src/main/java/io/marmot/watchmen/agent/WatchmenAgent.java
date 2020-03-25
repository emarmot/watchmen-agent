package io.marmot.watchmen.agent;

import java.lang.instrument.Instrumentation;

/***
 * 验证监监控Agent
 */
public class WatchmenAgent {

    /***
     * 参数列表
     * @param args  参数
     * @param inst
     */
    public static void premain(String args, Instrumentation inst){
            System.out.println("Watchmen Agent args:"+args);

            Class<?>[] classes=inst.getAllLoadedClasses();

            for(Class <?> clazz:classes){
                System.out.println(clazz.getClass());
            }
            System.out.println("Watchmen Agent end");
            // 添加Transformer
            inst.addTransformer(new WatchmenTransformer());
    }
}
