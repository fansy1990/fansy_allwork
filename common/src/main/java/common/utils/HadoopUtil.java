package common.utils;

import org.apache.hadoop.conf.Configuration;

import java.util.Map;

/**
 * Hadoop 工具类
 * Created by fansy on 2017/9/18.
 */
public class HadoopUtil {
    private static final String hadoop_properties ="hadoop.properties";
    private static Configuration conf = null;
    public static Configuration getConf() {
        if (conf == null) {
            Map<String,String> confMap = PropertiesUtil.getProperties(hadoop_properties);
            conf = new Configuration();
            conf.setBoolean("mapreduce.app-submission.cross-platform", Boolean
                    .parseBoolean(confMap
                            .get("mapreduce.app-submission.cross-platform")));// 配置使用跨平台提交任务
            conf.set("fs.defaultFS", confMap.get("fs.defaultFS"));// 指定namenode
            conf.set("mapreduce.framework.name",
                    confMap.get("mapreduce.framework.name")); // 指定使用yarn框架
            conf.set("yarn.resourcemanager.address",
                    confMap.get("yarn.resourcemanager.address")); // 指定resourcemanager
            conf.set("yarn.resourcemanager.scheduler.address",
                    confMap.get("yarn.resourcemanager.scheduler.address"));// 指定资源分配器
            conf.set("mapreduce.jobhistory.address",
                    confMap.get("mapreduce.jobhistory.address"));// 指定historyserver
//            conf.set("mapreduce.job.jar", JarUtil.jar(JarUtil.class));// 设置jar包路径
        }
        return conf;
    }

    /**
     * 向Configuration里面添加配置
     * 比如，更新mapreduce.job.jar的配置
     * @param key
     * @param value
     */
    public static void addPropertiesToConfiguration(String key,String value){
        if(conf == null){
            getConf();
        }
        conf.set(key,value);
    }
}
