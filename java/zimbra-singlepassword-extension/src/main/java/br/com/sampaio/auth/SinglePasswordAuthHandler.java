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

    @Override
    public void authenticate(Account account, String password, Map<String, Object> context, List<String> args) throws Exception {
        try {
			String contextProtocol = 
				context.get("proto") != null ? context.get("proto").toString() : null;
			
			ProtocolType protocol = ProtocolType.getByDescription(contextProtocol);

			if (protocol.equals(ProtocolType.SOAP))
			{
				String userAgent = 
					context.get("ua") != null ? context.get("ua").toString() : null;

				String clientIp = 
					context.get("ocip") != null ? context.get("ocip").toString() : null;

				// AuthContext.Protocol doesn't have SMTP mapped
				protocol = userAgent == null && clientIp == null ? 
					ProtocolType.SMTP :
					ProtocolType.OTHER;	
			}

			//regular auth
			if (protocol.equals(ProtocolType.OTHER))
			{
				Provisioning provisioningInstance = Provisioning.getInstance();
				AuthMechanism.doZimbraAuth((LdapProv) provisioningInstance, 
					provisioningInstance.getDomain(account), account, password, context);
			}
			else
			{
				//imap/smtp/pop3
				ZimbraLog.account.info("[SinglePasswordAuthHandler] account %s authenticating with single password", account);
				
				String dbHash = SinglePasswordTempStore.getInstance().getSingleAppPasswordHash(account.getName());
				
				String bcryptHashString = BCrypt.withDefaults().hashToString(12, password.toCharArray());
				
				if (password == null ||
						password.isEmpty() ||
						dbHash == null ||
						dbHash.isEmpty() ||
						!bcryptHashString.equals(dbHash))
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
