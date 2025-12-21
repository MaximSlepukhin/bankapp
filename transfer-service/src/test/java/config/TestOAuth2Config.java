package config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestOAuth2Config {

    @Bean
    @Primary
    public ClientRegistrationRepository mockClientRegistrationRepository() {
        return mock(ClientRegistrationRepository.class);
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientManager mockAuthorizedClientManager() {
        return mock(OAuth2AuthorizedClientManager.class);
    }

    @Bean
    @Primary
    public JwtDecoder mockJwtDecoder() {
        return mock(JwtDecoder.class);
    }
}
