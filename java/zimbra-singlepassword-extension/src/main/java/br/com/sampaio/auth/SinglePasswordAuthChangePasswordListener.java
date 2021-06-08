package br.com.sampaio.auth;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ldap.ChangePasswordListener;

public class SinglePasswordAuthChangePasswordListener extends ChangePasswordListener {

	public void register(String id) {
        ChangePasswordListener.register(id, this);
    }
	
	@Override
	public void postModify(Account arg0, String arg1, Map arg2) {
		
	}

	@Override
	public void preModify(Account arg0, String arg1, Map arg2, Map<String, Object> arg3) throws ServiceException {
		
	}

}
