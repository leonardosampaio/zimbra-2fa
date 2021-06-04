package br.com.sampaio;

import java.util.stream.Collectors;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;

import br.com.sampaio.auth.SinglePasswordAuthHandler;
import br.com.sampaio.auth.SinglePasswordAuthMech;

public class SinglePasswordExtension implements ZimbraExtension {
    // This string is used to refer to this extension
    public static final String ID = "singlepassword";
    
    public String getName() {
        return ID;
    }
    
    public void init() throws ExtensionException, ServiceException {
        new SinglePasswordAuthHandler().register(ID);
        
        final String s = Provisioning.getInstance().getAllDomains().stream()
            .filter(d -> new SinglePasswordAuthMech(d).isEnabled())
            .map(Domain::getName)
            .collect(Collectors.joining(", "));
        ZimbraLog.extensions.info("Single password authentication enabled for domains: %s", s.isEmpty() ? "(none)" : s);
    }

    public void destroy() {
        ZimbraLog.extensions.debug("Single password extension destroyed");
    }
}
