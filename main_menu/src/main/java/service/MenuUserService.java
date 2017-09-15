package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.MenuUser;
import org.apache.log4j.Logger;
import util.DBUtil;

public class MenuUserService {
    final static Logger logger = Logger.getLogger(MenuUserService.class);
	/**
	 * 根据MenuUser名获取用MenuUser实例
	 * 假设MenuUser的名字都不会重复
	 * @param name
	 * @return
	 */
	public MenuUser getMenuUserByName(String name) {
		String sql ="select * from menu_user where name='"+name+"'";
		List<MenuUser> menuUsers = queryWithResult(sql);
		return menuUsers.size()>0 ? menuUsers.get(0) : null;// 默认只返回第一个值
	}
	/**
	 * 通过sql查询，并得到结果
	 * @param sql
	 * @return
	 */
	public List<MenuUser> queryWithResult(String sql){
        logger.info("sql: "+sql);
		List<MenuUser> menuUsers = new ArrayList<>();
		Connection conn=null;
		PreparedStatement pstmt = null;
		ResultSet rs=null;// 定义返回集接收变量
		try {
			// 获取连接
			conn = DBUtil.getConn();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				menuUsers.add(new MenuUser(rs.getInt(1),rs.getString(2),rs.getInt(3),
						rs.getString(4),rs.getString(5)));
			}
			rs.close();
			pstmt.close();// 关闭资源
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return menuUsers;
	}
	
	/**
	 * 增加MenuUser操作
	 * @param menuUser
	 */
	public   boolean addMenuUser(MenuUser menuUser) {
		String sql ="insert into t_menuUser(name,age,gender,grade,password) "
				+ "values(?,?,?,?,?)";
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			// 获取连接
			conn = DBUtil.getConn();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, menuUser.getName());
			pstmt.setInt(2, menuUser.getAge());
			pstmt.setString(3, menuUser.getGender());
			pstmt.setString(4, menuUser.getPassword());
			pstmt.executeUpdate();
			
			pstmt.close();// 关闭资源
			conn.close();
			logger.info("MenuUser："+menuUser.getName()+"被插入数据库！");
			return true;
		}catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("MenuUser："+menuUser.getName()+"没有被插入数据库！");
		return false;
	}

}
