package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBUtil {
	// 定义static变量 MySQL数据库驱动类
	private static  String driver = null;
	// 定义MySQL连接地址
	public static  String url = "";
	// 定义数据库连接用户名
	public static  String user = "fansy";
	// 定义数据库连接密码
	public static  String password = "fansy";

    /**
     * 初始化 JDBC连接参数
     */
    private static void init(){
        Properties prop = new Properties();
        InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("/jdbc.properties");
        try {
            prop.load(in);
            driver = prop.getProperty("main.menu.jdbc.driver");
            url = prop.getProperty("main.menu.jdbc.url");
            user = prop.getProperty("main.menu.jdbc.user");
            password = prop.getProperty("main.menu.jdbc.password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * 获取数据库连接
	 * @return
	 */
	public static Connection getConn() {
        if(driver == null || url == null || user == null || password == null){
            init();
        }

		Connection conn=null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url,user,password);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
}
