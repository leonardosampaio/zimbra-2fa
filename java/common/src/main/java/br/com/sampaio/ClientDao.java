package br.com.sampaio;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ClientDao {

	private String mysqlUrl;
	private String mysqlUser;
	private String mysqlPassword;
	private Connection conn;
	private Map<String, String> domains; //httpsDomain:zimbraDomain
	
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

		String domainsStr = prop.getProperty("domains");
		domains = new HashMap<>();
		if (!domainsStr.isEmpty())
		{
			for (String domain : domainsStr.split(" "))
			{
				String[] zimbraDomainHttpsDomain = domain.split(":");
				domains.put(zimbraDomainHttpsDomain[1].toLowerCase(), zimbraDomainHttpsDomain[0].toLowerCase());
			}
		}
		
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
		String sql = "insert into clients (email,secret_key,validated) values (?,?,false)";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email);
		preparedStatement.setString(2, secretKey);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}
	
	public List<SecretKeyWrapper> getSecretKey(String httpsDomain, String loginOrFullEmail) throws SQLException
	{
		List<SecretKeyWrapper> result = new ArrayList<>();

		if (this.domains.isEmpty() || !this.domains.containsKey(httpsDomain.toLowerCase()))
		{
			return result;
		}

		String sql = "select secret_key, validated from clients where lower(email) = ?";
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, loginOrFullEmail.toLowerCase().split("@")[0] + "@" + this.domains.get(httpsDomain.toLowerCase()));
		ResultSet resultSet = preparedStatement.executeQuery();
		
		while (resultSet.next())
		{
			result.add(new SecretKeyWrapper(
					resultSet.getString("secret_key"),
					resultSet.getBoolean("validated")));
		}
		
		resultSet.close();
		preparedStatement.close();
		this.closeConnection();
		
		return result;
	}

	public void validateSecretKey(String httpsDomain, String loginOrFullEmail, String secretKey) throws SQLException
	{
		String sql = "update clients set validated = true where lower(email) = ? "
				+ " and secret_key = ?";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, loginOrFullEmail.toLowerCase().split("@")[0] + "@" + this.domains.get(httpsDomain.toLowerCase()));
		preparedStatement.setString(2, secretKey);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}

	public void invalidateSecretKey(String email) throws SQLException
	{
		String sql = "update clients set validated = false, secret_key = ''  "
				+ "where lower(email) = ?";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}

	public boolean hasValidSecretKey(String httpsDomain, String loginOrFullEmail) throws SQLException
	{
		boolean result = false;
		
		String sql = "select validated from clients where "
				+ "lower(email) = ? and validated = true";
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, loginOrFullEmail.toLowerCase().split("@")[0] + "@" + this.domains.get(httpsDomain.toLowerCase()));
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

	public void putSingleAppPasswordHash(String email, String bCryptHashString) throws SQLException
	{
		String sql = "insert into single_app_password (email, hash, in_use) values (?,?,false)";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email);
		preparedStatement.setString(2, bCryptHashString);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}
	
	public List<String> getSingleAppPasswordHash(String email) throws SQLException
	{
		List<String> result = new ArrayList<>();
		
		String sql = "select hash from single_app_password "
				+ "where lower(email) = ?";
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
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
				+ "where lower(email) = ?";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.executeUpdate();
		preparedStatement.close();
		this.closeConnection();
	}

	public void setHashInUse(String email, String hash) throws SQLException
	{
		String sql = "update single_app_password set in_use = true  "
						+ "where lower(email) = ? and hash = ?";
		
		this.openConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setString(1, email.toLowerCase());
		preparedStatement.setString(2, hash);
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