package br.com.sampaio.auth;

import java.util.List;
import java.util.Map;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.auth.AuthMechanism;
import com.zimbra.cs.account.auth.ZimbraCustomAuth;
import com.zimbra.cs.account.ldap.LdapProv;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.sampaio.SinglePasswordTempStore;

public class SinglePasswordAuthHandler extends ZimbraCustomAuth {

    public void register(String id) {
        ZimbraCustomAuth.register(id, this);
    }

    /**
     * Authentication with bcrypt hashs.
     * 
     * Admin users fall back to zimbra auth in case of failure.
     * 
     * @see LdapProvisioning.verifyPasswordInternal
     */
    @Override
    public void authenticate(Account account, String password, Map<String, Object> context, List<String> args) throws Exception {
        try {
			String contextProtocol = 
				context != null && context.get("proto") != null ? context.get("proto").toString() : null;
			
			ZimbraLog.account.info("[SinglePasswordAuthHandler] contextProtocol: %s", contextProtocol);

			ProtocolType protocol = ProtocolType.getByDescription(contextProtocol);

			ZimbraLog.account.info("[SinglePasswordAuthHandler] protocol: %s", protocol.toString());

			if (protocol.equals(ProtocolType.SOAP))
			{
				String userAgent = 
					context.get("ua") != null ? context.get("ua").toString() : null;

				ZimbraLog.account.info("[SinglePasswordAuthHandler] userAgent: %s", userAgent);

				// AuthContext.Protocol doesn't have SMTP mapped
				protocol = ProtocolType.OTHER;
				if (userAgent == null)
				{
					protocol = ProtocolType.SMTP;
				}

				ZimbraLog.account.info("[SinglePasswordAuthHandler] considering SOAP protocol as: %s", protocol.toString());
			}
			
			//empty for non-2fa users
			List<String> hashs = SinglePasswordTempStore.getInstance().getSingleAppPasswordsHashs(account.getName());
			
			//regular auth
			if (protocol.equals(ProtocolType.OTHER) || hashs == null || hashs.isEmpty())
			{
				ZimbraLog.account.info("[SinglePasswordAuthHandler] account %s authenticating with AuthMechanism.doZimbraAuth", account);
				Provisioning provisioningInstance = Provisioning.getInstance();
				AuthMechanism.doZimbraAuth((LdapProv) provisioningInstance, 
					provisioningInstance.getDomain(account), account, password, context);
			}
			else
			{
				//imap/smtp/pop3
				ZimbraLog.account.info("[SinglePasswordAuthHandler] account %s authenticating with single password", account);
				
				boolean verified = false;
				if (password != null && !password.isEmpty() && hashs != null && !hashs.isEmpty())
				{
					for (String hash : hashs)
					{
						if(BCrypt.verifyer().verify(password.toCharArray(), hash).verified)
						{
							SinglePasswordTempStore.getInstance().setSinglePasswordHashInUse(account.getName(), hash);
							verified = true;
							break;
						}
					}
				}
				
				if (!verified)
            	{
					throw new SinglePasswordException(
						String.format("[SinglePasswordAuthHandler] Invalid single password for account %s",
							account));
				}
			}
        }
        catch (SinglePasswordException e) {
            ZimbraLog.account.debug("[SinglePasswordAuthHandler] Authentication failed: %s", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            ZimbraLog.account.error("[SinglePasswordAuthHandler] Unexpected %s: %s", e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
    }
}
