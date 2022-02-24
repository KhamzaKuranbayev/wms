package uz.uzcard.genesis.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.GrantType;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hibernate.Cache;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.dto.api.req.setting.JwtRequest;
import uz.uzcard.genesis.dto.auth.AuthUserRefreshTokenDto;
import uz.uzcard.genesis.dto.auth.SessionDto;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.UserDao;
import uz.uzcard.genesis.hibernate.dao.UserSessionDao;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.hibernate.entity._UserSession;
import uz.uzcard.genesis.service.AccountService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    //    public static String OAUTH_AUTH_URL = "http://localhost:9991/oauth/token";
    @Value("${oauth2.clientId}")
    private String clientId;
    @Value("${oauth2.clientSecret}")
    private String clientSecret;
    @Value("${server.oauth.url}")
    private String oauthUrl;

    @Autowired
    private UserDao userDao;
    @Autowired
    private UserSessionDao userSessionDao;
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public _UserSession createSession(Authentication authentication, HttpServletRequest request, String sessionId) {
        SessionUtils.getInstance().setSessionId(sessionId);
        _User user = userDao.getByUseName(authentication.getName());
        _UserSession userSession = userSessionDao.getBySessionId(sessionId);
        if (userSession == null)
            userSession = new _UserSession();
        userSession.setUser(user);
        userSession.setToken(sessionId);
        userSession.setIPAddress(getClientIpAddr(request));
        userSession.setSigniinDate(Instant.now());
        userSession.setUuid(UUID.randomUUID().toString());
        userSessionDao.save(userSession);
        return userSession;
    }

    public String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("HTTP_X_FORWARDED");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("HTTP_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("HTTP_FORWARDED");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("HTTP_VIA");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("REMOTE_ADDR");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getRemoteAddr();
        }
        System.out.println(ip);
        return ip;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Override
    public UserDetails loadUserByUsername(String username) {
        _User user = userDao.getByUseName(username);
        if (user == null) {
            throw new BadCredentialsException(GlobalizationExtentions.localication("USERNAME_OR_PASSWORD_WRONG"));
        }
        if (!user.isActived()) {
            throw new LockedException(GlobalizationExtentions.localication("BLOCK_ACCOUNT"));
        }
        return user;
    }

    @Override
    public SessionDto login(JwtRequest authenticationRequest, HttpServletRequest request) throws Exception {
        try {
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpPost httppost = new HttpPost(oauthUrl + "/token");

            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("grant_type", GrantType.PASSWORD.getValue()));
            nameValuePairs.add(new BasicNameValuePair("username", authenticationRequest.getUserName()));
            nameValuePairs.add(new BasicNameValuePair("password", authenticationRequest.getPassword()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httppost.addHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
            httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            HttpResponse response = httpclient.execute(httppost);
            return getAuthDtoDataDto(authenticationRequest, response, true, request);

        } catch (Exception ex) {
            throw new ValidatorException(ex.getMessage());
        }
    }

    @Override
    public SessionDto refreshToken(AuthUserRefreshTokenDto refreshToken, HttpServletRequest request) {
        try {
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpPost httppost = new HttpPost(oauthUrl + "/token");

            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("grant_type", GrantType.REFRESH_TOKEN.getValue()));
            nameValuePairs.add(new BasicNameValuePair("refresh_token", refreshToken.getRefreshToken()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            httppost.addHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
            httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            HttpResponse response = httpclient.execute(httppost);

            return getAuthDtoDataDto(new JwtRequest(), response, false, request);

        } catch (Exception ex) {
            throw new ValidatorException(ex.getMessage());
        }
    }

    @Override
    public void reloadCache() {
        Session session = userDao.getSession();
        if (session != null) {
            session.clear(); // internal cache clear
        }
        Cache cache = sessionFactory.getCache();
        if (cache != null) {
            cache.evictAllRegions(); // Evict data from all query regions.
        }
    }

    private SessionDto getAuthDtoDataDto(JwtRequest authenticationRequest, HttpResponse response, boolean authentication, HttpServletRequest request) throws IOException {

        JsonNode json_auth = new ObjectMapper().readTree(EntityUtils.toString(response.getEntity()));

        if (!json_auth.has("error")) {
            String token = json_auth.get("access_token").asText();

            SessionDto authDto = new SessionDto();
            authDto.setExpiresIn(json_auth.get("expires_in").asLong());
            authDto.setSessionToken(token);
            authDto.setRefreshToken(json_auth.get("refresh_token").asText());
            authDto.setTokenType(json_auth.get("token_type").asText());
            authDto.setScope(json_auth.get("scope").asText());

            return authDto;
        } else {
            String error_description = json_auth.has("error_description") ? json_auth.get("error_description").asText() : null;
            if (error_description == null || error_description.isEmpty()) {
                error_description = "USERNAME_OR_PASSWORD_WRONG";
            } else if (error_description.contains("NoResultException")) {
                error_description = "USERNAME_OR_PASSWORD_OR_BLOCK_ACCOUNT";
            }
            throw new RuntimeException(error_description);
        }
    }

    private String getAuthorization() {
        return "Basic " + ServerUtils.encodeToBase64(clientId + ":" + clientSecret);
    }
}