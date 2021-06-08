package br.com.sampaio;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class SinglePasswordTempStore {
    
    private static SinglePasswordTempStore instance;

    private Map<String, String> tempStore;

    private SinglePasswordTempStore(){
        tempStore = new HashMap<>();
    }

    public static SinglePasswordTempStore getInstance()
    {
        if (instance == null) {
            instance = new SinglePasswordTempStore();
        }
        return instance;
    }

    public void invalidatePassword(String email) throws IOException, SQLException
    {
        new ClientDao().invalidateSingleAppPasswordHash(email);
        tempStore.put(email, null);
    }
    
    /**
     * Get password in memory.
     * 
     * @param email
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public String getPassword(String email) throws IOException, SQLException
    {
        String password = null;
    	if (tempStore.get(email) == null)
    	{
            password = new RandomString(8, ThreadLocalRandom.current()).nextString();
    		String bcryptHashString = BCrypt.withDefaults().hashToString(12, password.toCharArray());
    		new ClientDao().putSingleAppPasswordHash(email, bcryptHashString);
    		tempStore.put(email, password);
    	}
        else {
            password = tempStore.get(email);
        }
        
    	return password;
    }

	public List<String> getSingleAppPasswordHash(String name) throws SQLException, IOException {
		return new ClientDao().getSingleAppPasswordHash(name);
	}
}