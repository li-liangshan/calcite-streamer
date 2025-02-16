package com.streamer.core.conn;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.lang3.StringUtils;

public class JdbcConnectionPool {

	private static Map<String, Connection> connections = new ConcurrentHashMap<String, Connection>();

	public static Connection getConnection(String driver, String url, String user, String password) {
		String key = DigestUtils.md5Hex(StringUtils.join(driver, url, user, password).getBytes());
		try {
			Class.forName(driver);
			if (connections.containsKey(key)) {
				Connection connection = connections.get(key);
				if (connection == null || connection.isClosed()) {
					connection = new DriverManagerConnectionFactory(url, user, password).createConnection();
					connections.putIfAbsent(key, connection);
				}
				return connection;
			} else {
				Connection connection = new DriverManagerConnectionFactory(url, user, password).createConnection();
				connections.putIfAbsent(key, connection);
				return connection;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void cleanAll() {
		// 故意不关闭，等待系统回收.多线程中,不确定其他线程是否占用.
		connections.clear();
	}

}
