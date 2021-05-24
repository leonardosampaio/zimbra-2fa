package br.com.sampaio;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class ClientDao {

	private String mysqlUrl;
	private String mysqlUser;
	private String mysqlPassword;
	private Connection conn;
	
	protected ClientDao() throws IOException, SQLException
	{
		String configFile = System.getenv("2FA_CONFIG_FILE");

		if (configFile == null || configFile.isEmpty())
		{
			configFile = "/opt/zimbra/2fa/config.properties";
		}

		Properties prop = new Properties();
		FileInputStream input = new FileInputStream(configFile);
		prop.load(input);
		
		mysqlUrl = prop.getProperty("mysqlUrl");
		mysqlUser = prop.getProperty("mysqlUser");
		mysqlPassword = prop.getProperty("mysqlPassword");
		
		input.close();
	}
	
	private void openConnection() throws SQLException
	{
		if (conn == null || conn.isClosed())
		{
			conn = DriverManager.getConnection(
					mysqlUrl,
					mysqlUser,
					mysqlPassword);
		}
	}
	
	private void closeConnection() throws SQLException
	{
		if (conn != null && !conn.isClosed())
		{
			conn.close();
		}
	}
	
	public void putSecretKey(String email, String secretKey) throws SQLException
	{
		String sql = "insert into clients (email,secret_key,validated) values (?,?,false)"
				+ " on duplicate key update secret_key = ?, validated = false";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email);
		preparedStatement.setString(2, secretKey);
		preparedStatement.setString(3, secretKey);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}
	
	public SecretKeyWrapper getSecretKey(String email) throws SQLException
	{
		SecretKeyWrapper result = null;
		
		String sql = "select secret_key, validated from clients where email = ?";
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		if (resultSet.next())
		{
			result = new SecretKeyWrapper(
					resultSet.getString("secret_key"),
					resultSet.getBoolean("validated"));
		}
		
		resultSet.close();
		preparedStatement.close();
		this.closeConnection();
		
		return result;
	}

	public void validateSecretKey(String email, String secretKey) throws SQLException
	{
		String sql = "update clients set validated = true where email = ? and secret_key = ?";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email);
		preparedStatement.setString(2, secretKey);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}

	public void invalidateSecretKey(String email) throws SQLException
	{
		String sql = "update clients set validated = false, secret_key = null  where email = ?";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}

	public boolean hasValidSecretKey(String email) throws SQLException
	{
		boolean result = false;
		
		String sql = "select validated from clients where email = ? and validated = true";
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		if (resultSet.next())
		{
			result = resultSet.getBoolean("validated");
		}
		
		resultSet.close();
		preparedStatement.close();
		this.closeConnection();
		
		return result;
	}
}