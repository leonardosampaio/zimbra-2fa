/*******************************************************************************
 * Copyright 2018, 2019 Silpion IT-Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/Apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.sampaio.auth;

import java.util.List;
import java.util.Map;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.auth.AuthMechanism;
import com.zimbra.cs.account.auth.ZimbraCustomAuth;
import com.zimbra.cs.account.ldap.LdapProv;

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
				if (password == null || password.isEmpty() || !password.equals("teste"))
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
