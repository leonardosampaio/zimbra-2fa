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

    /**
     * Admin zimlet > invalidate.jsp calls this
     * 
     * @param email
     * @throws IOException
     * @throws SQLException
     */
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
        String password = tempStore.get(email);
        
    	if (password == null || isSinglePasswordInUse(email, password))
    	{
            password = new RandomString(16, ThreadLocalRandom.current()).nextString();
    		String bcryptHashString = BCrypt.withDefaults().hashToString(12, password.toCharArray());
    		new ClientDao().putSingleAppPasswordHash(email, bcryptHashString);
    		tempStore.put(email, password);
    	}
        else {
            password = tempStore.get(email);
        }
        
    	return password;
    }
    
    /**
     * Avoid generating new temporary password if the current one is unused. 
     * @param email
     * @param password
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private boolean isSinglePasswordInUse(String email, String password) throws SQLException, IOException
    {
    	List<String> hashs = this.getSingleAppPasswordsHashs(email);
    	
    	if (hashs != null && !hashs.isEmpty())
    	{
    		for (String hash : hashs)
    		{
    			if(BCrypt.verifyer().verify(password.toCharArray(), hash).verified && 
    					new ClientDao().isHashInUse(email, hash))
    			{
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }

	public List<String> getSingleAppPasswordsHashs(String email) throws SQLException, IOException {
		return new ClientDao().getSingleAppPasswordHash(email);
	}
	
	/**
	 * Hash is marked is in use to allow jsp calls to generate a new temporary password.
	 * 
	 * @param email
	 * @param hash
	 * @throws SQLException
	 * @throws IOException
	 */
	public void setSinglePasswordHashInUse(String email, String hash) throws SQLException, IOException
	{
		new ClientDao().setHashInUse(email, hash);
	}
}