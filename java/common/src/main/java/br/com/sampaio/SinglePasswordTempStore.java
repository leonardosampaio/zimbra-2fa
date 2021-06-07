package br.com.sampaio;

import java.util.HashMap;
import java.util.Map;

public class SinglePasswordTempStore {
    
    private static SinglePasswordTempStore instance;

    private Map<String, KeyStore> tempStore;

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
    
    public String getPassword(String account, AccessType type)
    {
    	if (tempStore.get(account) == null)
    	{
    		tempStore.put(account, new KeyStore());
    	}
    	
    	return tempStore.get(account).getPassword(type);
    }
}