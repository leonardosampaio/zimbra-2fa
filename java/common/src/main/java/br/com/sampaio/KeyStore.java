package br.com.sampaio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KeyStore {
	
	private String password;
	private List<AccessType> entries;
	
	public KeyStore() {
		super();
		
	    this.password = new RandomString(8, ThreadLocalRandom.current()).nextString();
	    
		this.entries = new ArrayList<AccessType>();
		entries.add(AccessType.WEB);
		entries.add(AccessType.AUTH);
	}
	
	public String getPassword(AccessType type)
	{
		if (entries.contains(type))
		{
			entries.remove(type);
			return password;
		}
		return null;
	}
}
