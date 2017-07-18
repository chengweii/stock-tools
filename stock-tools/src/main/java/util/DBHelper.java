package util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DBHelper {
	public static  String url = "jdbc:mysql://127.0.0.1/stock?characterEncoding=utf-8";
	public static  String driver = "com.mysql.jdbc.Driver";  
    public static  String user = "root";  
    public static  String password = "root";
	/**
	 * 根据字符串选择数据源
	 * @param dataSource
	 * @return
	 */
	public static Connection getConn() {
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static int excuteUpdate(Connection conn, String sql, String[] params) {
		int flag = 0;
		if (conn == null)
			return flag;
		PreparedStatement ps;

		try {
			ps = conn.prepareStatement(sql);
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					ps.setObject(i + 1, params[i]);
				}
			}
			flag = ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("----------------------");
			System.out.println("执行SQL:"+sql);
			if (params != null) {
				String msg="[";
				for (int i = 0; i < params.length; i++) {
					msg+=params[i]+" ";
				}
				msg+="]";
				System.out.println("参数:"+msg);
			}
			
			System.out.println("错误信息："+e.getMessage());
		}
		return flag;
	}
	
	public static int excuteUpdateNoCatch(Connection conn, String sql,
			String[] params) throws Exception {
		int flag = 0;
		if (conn == null)
			return flag;
		PreparedStatement ps;

		ps = conn.prepareStatement(sql);
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}
		}
		flag = ps.executeUpdate();
		return flag;
	}
	
	public static void excuteCall(Connection conn, String sql, String[] params) {
		CallableStatement cs;

		try {
			cs = conn.prepareCall(sql);
			if (params != null) {
				for (int i = 0; i < params.length; i++)
					cs.setObject(i + 1, params[i]);
			}
			cs.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static ResultSet excuteQuery(Connection conn, String sql, String[] params) {
		ResultSet rs = null;
		if (conn == null)
			return rs;
		PreparedStatement ps;

		try {
			ps = conn.prepareStatement(sql);
			if (params != null) {
				for (int i = 0; i < params.length; i++)
					ps.setObject(i + 1, params[i]);
			}
			rs = ps.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	public void update(Connection conn,String sql, String[] params) {
		try {
			excuteUpdate(conn, sql, params);
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static List<Map<String,Object>> queryForList(String sql,String[] params){
		Connection conn = DBHelper.getConn();
		ResultSet detailrs = DBHelper.excuteQuery(conn, sql, params);
		List<Map<String, Object>> detaillist = DBHelper.rsToMapList(detailrs);
		return detaillist;
	}
	
	public static List<Map<String,Object>> rsToMapList(ResultSet rs){
		List<Map<String,Object>> datalist=new ArrayList<Map<String,Object>>();
		try {
			ResultSetMetaData md=rs.getMetaData();
			Map<String,Object> rowdata;
			int columnCount=md.getColumnCount();
			int i;
			while (rs.next()) {
				rowdata=new LinkedHashMap<String,Object>();
				for(i=1;i<=columnCount;i++)
					rowdata.put(md.getColumnName(i), rs.getObject(i));
				datalist.add(rowdata);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return datalist;
	}
}
