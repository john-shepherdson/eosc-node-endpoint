package gr.uoa.di.madgik.node.config;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityPropertiesTest {

    @Test
    void acceptsCompleteConfiguration() {
        SecurityProperties properties = new SecurityProperties();
        properties.setAdminEmails(Set.of("admin@eosc-beyond.eu"));
        properties.setLoginRedirect("http://localhost:4200");
        properties.setLogoutRedirect("http://localhost:4200");

        assertThatCode(properties::afterPropertiesSet).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingAdminEmails() {
        SecurityProperties properties = new SecurityProperties();

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("security.admin-emails must contain at least one email address");
    }

    @Test
    void rejectsBlankRedirects() {
        SecurityProperties properties = new SecurityProperties();
        properties.setAdminEmails(Set.of("admin@eosc-beyond.eu"));
        properties.setLoginRedirect(" ");

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("security.login-redirect must not be empty");
    }
}
