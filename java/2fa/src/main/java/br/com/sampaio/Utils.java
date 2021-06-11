package br.com.sampaio;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.sql.SQLException;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import de.taimos.totp.TOTP;

public class Utils {

	private ClientDao dao;
	
	public Utils() throws IOException, SQLException
	{
		this.dao = new ClientDao();
	}
	
	/**
	 * Avoid resubmission of secretKey by client zimlet.
	 * 
	 * @param email
	 * @return
	 * @throws SQLException 
	 */
	public boolean hasValidSecretKey(String email) throws SQLException
	{
		return dao.hasValidSecretKey(email);
	}
	
	/**
	 * Client Google Authenticator installation step.
	 * 
	 * @param companyName
	 * @param email
	 * @return
	 * @throws WriterException
	 * @throws IOException
	 * @throws SQLException 
	 */
	public String getQrCodeB64(String companyName, String email, int qrCodeWidth, int qrCodeHeight) throws WriterException, IOException, SQLException
	{
		String secretKey = this.generateSecretKey();
        
        dao.putSecretKey(email, secretKey);
        
        String barCodeUrl = this.getGoogleAuthenticatorBarCode(secretKey, email, companyName);
        BitMatrix matrix = new MultiFormatWriter().encode(
        		barCodeUrl,
        		BarcodeFormat.QR_CODE,
        		qrCodeWidth,
        		qrCodeHeight);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(matrix, "png", out);
            return Base64.encodeBase64String(out.toByteArray());
        }
	}
	
	/**
	 * Validate Google Authenticator code.
	 * 
	 * @param code
	 * @param secretKey
	 * @return
	 * @throws SQLException 
	 */
	public boolean validateCode(String email, String code) throws SQLException
	{
		SecretKeyWrapper wrapper = dao.getSecretKey(email);
		boolean valid = code.equals(getTOTPCode(wrapper.getSecretKey()));
		
		if (valid && !wrapper.isValidated())
		{
			//From this point secretKey should change only if admin zimlet calls invalidateSecretKey
			dao.validateSecretKey(email, wrapper.getSecretKey());
		}
		
		return valid;
	}
	
	/**
	 * Admin zimlet calls this to reset client install.
	 * 
	 * @param email
	 * @throws SQLException 
	 */
	public void invalidateSecretKey(String email) throws SQLException
	{
		dao.invalidateSecretKey(email);
	}

	public void changePassword(String email, String password) throws IOException, InterruptedException
	{
		Process zmprovProcess = Runtime.getRuntime().exec(
				String.format("/opt/zimbra/bin/zmprov setPassword %s %s", email, password));
		zmprovProcess.waitFor();
	}
	
	private String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) {
        try {
			final String encoding = "UTF-8";
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + ":" + account, encoding).replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, encoding).replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, encoding).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
	
    private String generateSecretKey()
    {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    private String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }
}