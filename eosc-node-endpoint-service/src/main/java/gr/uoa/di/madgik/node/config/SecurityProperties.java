package gr.uoa.di.madgik.node.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties implements InitializingBean {

    private Set<String> adminEmails = new LinkedHashSet<>();
    private String loginRedirect = "";
    private String logoutRedirect = "";

    public Set<String> getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(Set<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    public String getLoginRedirect() {
        return loginRedirect;
    }

    public void setLoginRedirect(String loginRedirect) {
        this.loginRedirect = loginRedirect;
    }

    public String getLogoutRedirect() {
        return logoutRedirect;
    }

    public void setLogoutRedirect(String logoutRedirect) {
        this.logoutRedirect = logoutRedirect;
    }

    @Override
    public void afterPropertiesSet() {
        if (adminEmails == null || adminEmails.isEmpty()) {
            throw new IllegalStateException("security.admin-emails must contain at least one email address");
        }
        if (!StringUtils.hasText(loginRedirect)) {
            throw new IllegalStateException("security.login-redirect must not be empty");
        }
        if (!StringUtils.hasText(logoutRedirect)) {
            throw new IllegalStateException("security.logout-redirect must not be empty");
        }
    }
}
