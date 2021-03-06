package br.com.sampaio.auth;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.zimbra.common.util.QuotedStringParser;
import com.zimbra.cs.account.Domain;

import br.com.sampaio.SinglePasswordExtension;

public class SinglePasswordAuthMech {
    private static final String AUTH_MECH_PREFIX = "custom:" + SinglePasswordExtension.ID;
    
    private static final String SPACE = " ";

    private final String config;

    public SinglePasswordAuthMech(Domain domain) {
        config = Optional.ofNullable(domain.getAuthMech()).orElse("");
    }
    
    public boolean isEnabled() {
        if (!config.startsWith(AUTH_MECH_PREFIX)) {
            return false;
        }
        final int l = AUTH_MECH_PREFIX.length();
        return config.length() == l || config.substring(l, l + 1).equals(SPACE);
    }
    
    public List<String> getArgs() {
        if (!hasArgs()) {
            return Collections.emptyList();
        }
        return parseArgString(config.substring(AUTH_MECH_PREFIX.length() + SPACE.length()));
    }

    private boolean hasArgs() {
        return isEnabled() && config.length() > AUTH_MECH_PREFIX.length();
    }

    private static List<String> parseArgString(String args) {
        return new QuotedStringParser(args).parse();
    }
}
