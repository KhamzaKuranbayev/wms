package uz.uzcard.genesis.uitls;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.auth.SessionDto;

public class AuthUtils {

    public static SessionDto onSuccessSession(OAuth2AccessToken oAuth2AccessToken) {

        String token = oAuth2AccessToken.getValue();
        String token_type = oAuth2AccessToken.getTokenType();
        String refresh_token = oAuth2AccessToken.getRefreshToken().getValue();
        Long expires_in = (long) oAuth2AccessToken.getExpiresIn();
        String scope = "";

        return SessionDto.builder()
                .sessionToken(token).tokenType(token_type).refreshToken(refresh_token)
                .expiresIn(expires_in).scope(scope).build();
    }
}
