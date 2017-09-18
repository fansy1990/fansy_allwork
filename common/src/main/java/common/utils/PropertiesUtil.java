package common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 处理Properties文件
 * Created by fansy on 2017/9/16.
 */
public class PropertiesUtil {
    private static final Logger log = LoggerFactory.getLogger(PropertiesUtil.class);
    public static Map<String, String> getProperties(String file) {
        log.info("loading file :"+file);
        Map<String, String> props = new HashMap<>();
        Properties prop = new Properties();
        InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream(file);
        try {
            prop.load(in);
            Enumeration en = prop.propertyNames(); //得到配置文件的名字

            while (en.hasMoreElements()) {
                String strKey = (String) en.nextElement();
                String strValue = prop.getProperty(strKey);
                props.put(strKey, strValue);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }
}
