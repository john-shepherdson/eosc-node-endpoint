/*
 * Copyright 2026 OpenAIRE AMKE & Athena Research and Innovation Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.uoa.di.madgik.node;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@SpringBootTest
class NodeEndpointApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class OAuth2TestConfiguration {

		@Bean
		ClientRegistrationRepository clientRegistrationRepository() {
			ClientRegistration registration = ClientRegistration.withRegistrationId("eosc")
					.clientId("test-client")
					.clientSecret("test-secret")
					.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
					.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
					.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
					.scope("openid", "email", "profile")
					.authorizationUri("https://eosc-beyond.eu/oauth2/authorize")
					.tokenUri("https://eosc-beyond.eu/oauth2/token")
					.jwkSetUri("https://eosc-beyond.eu/oauth2/jwks")
					.userInfoUri("https://eosc-beyond.eu/oauth2/userinfo")
					.userNameAttributeName("sub")
					.clientName("EOSC")
					.build();

			return new InMemoryClientRegistrationRepository(registration);
		}

		@Bean
		JwtDecoder jwtDecoder() {
			return token -> Jwt.withTokenValue(token)
					.header("alg", "none")
					.claim("sub", "test-user")
					.build();
		}
	}
}
