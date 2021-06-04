package br.com.sampaio;

public class SecretKeyWrapper {

	private String secretKey;
	private boolean validated;
	
	public SecretKeyWrapper(String secretKey, boolean validated) {
		super();
		this.secretKey = secretKey;
		this.validated = validated;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public boolean isValidated() {
		return validated;
	}
}
