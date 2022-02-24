package uz.uzcard.genesis.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import uz.uzcard.genesis.dto.api.req.setting.JwtRequest;
import uz.uzcard.genesis.dto.auth.AuthUserRefreshTokenDto;
import uz.uzcard.genesis.dto.auth.SessionDto;
import uz.uzcard.genesis.hibernate.entity._UserSession;

import javax.servlet.http.HttpServletRequest;

public interface AccountService extends UserDetailsService {
    _UserSession createSession(Authentication authentication, HttpServletRequest request, String sessionId);

    SessionDto login(JwtRequest authenticationRequest, HttpServletRequest request) throws Exception;

    SessionDto refreshToken(AuthUserRefreshTokenDto refreshToken, HttpServletRequest request);

    void reloadCache();
}
