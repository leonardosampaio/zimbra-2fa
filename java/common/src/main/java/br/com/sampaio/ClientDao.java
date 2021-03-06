package br.com.sampaio;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ClientDao {

	private String mysqlUrl;
	private String mysqlUser;
	private String mysqlPassword;
	private Connection conn;
	
	protected ClientDao() throws IOException, SQLException
	{
		String configFile = System.getenv("2FA_CONFIG_FILE_PATH");

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
		
		String sql = "select secret_key, validated from clients "
				+ "where (lower(email) = ? or "
				+ "lower(SUBSTR(email,1,LOCATE('@',email)-1)) = ?)";
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.setString(2, email.toLowerCase().split("@")[0]);
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
		String sql = "update clients set validated = true where "
				+ "(lower(email) = ? or lower(SUBSTR(email,1,LOCATE('@',email)-1)) = ?) "
				+ "and secret_key = ?";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.setString(2, email.toLowerCase().split("@")[0]);
		preparedStatement.setString(3, secretKey);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}

	public void invalidateSecretKey(String email) throws SQLException
	{
		String sql = "update clients set validated = false, secret_key = ''  "
				+ "where (lower(email) = ? or lower(SUBSTR(email,1,LOCATE('@',email)-1)) = ?)";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.setString(2, email.toLowerCase().split("@")[0]);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}

	public boolean hasValidSecretKey(String email) throws SQLException
	{
		boolean result = false;
		
		String sql = "select validated from clients where "
				+ "(lower(email) = ? or lower(SUBSTR(email,1,LOCATE('@',email)-1)) = ?) "
				+ "and validated = true";
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.setString(2, email.toLowerCase().split("@")[0]);
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

	public void putSingleAppPasswordHash(String email, String bcryptHashString) throws SQLException
	{
		String sql = "insert into single_app_password (email, hash, in_use) values (?,?,false)";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email);
		preparedStatement.setString(2, bcryptHashString);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}
	
	public List<String> getSingleAppPasswordHash(String email) throws SQLException
	{
		List<String> result = new ArrayList<>();
		
		String sql = "select hash from single_app_password "
				+ "where (lower(email) = ? or "
				+ "lower(SUBSTR(email,1,LOCATE('@',email)-1)) = ?)";
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.setString(2, email.toLowerCase().split("@")[0]);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		while(resultSet.next())
		{
			result.add(resultSet.getString("hash"));
		}
		
		resultSet.close();
		preparedStatement.close();
		this.closeConnection();
		
		return result;
	}

	public void invalidateSingleAppPasswordHash(String email) throws SQLException
	{
		String sql = "delete from single_app_password "
				+ "where (lower(email) = ? or lower(SUBSTR(email,1,LOCATE('@',email)-1)) = ?)";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.setString(2, email.toLowerCase().split("@")[0]);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}

	public void setHashInUse(String email, String hash) throws SQLException
	{
		String sql = "update single_app_password set in_use = true  "
				+ "where (lower(email) = ? or lower(SUBSTR(email,1,LOCATE('@',email)-1)) = ?) "
				+ "and hash = ?";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.setString(2, email.toLowerCase().split("@")[0]);
		preparedStatement.setString(3, hash);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}

	public boolean isHashInUse(String email, String hash) throws SQLException
	{
		boolean result = false;
		
		String sql = "select in_use from single_app_password where "
				+ "(lower(email) = ? or lower(SUBSTR(email,1,LOCATE('@',email)-1)) = ?) "
				+ "and hash = ?";
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.setString(2, email.toLowerCase().split("@")[0]);
		preparedStatement.setString(3, hash);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		if (resultSet.next())
		{
			result = resultSet.getBoolean("in_use");
		}
		
		resultSet.close();
		preparedStatement.close();
		this.closeConnection();
		
		return result;
	}
}