package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.setting.JwtRequest;
import uz.uzcard.genesis.dto.api.resp.JwtResponse;
import uz.uzcard.genesis.dto.auth.AuthUserRefreshTokenDto;
import uz.uzcard.genesis.dto.auth.SessionDto;
import uz.uzcard.genesis.service.AccountService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "Authentication", tags = "0 Authentication")
@RestController
@CrossOrigin
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AccountService accountService;
    @Resource(name = "tokenServices")
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @ApiOperation(value = "login", response = JwtResponse.class)
    @Transactional
    @RequestMapping(value = "/api/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest, HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
        SessionDto token = null;
        try {
            token = accountService.login(authenticationRequest, request);
        } catch (Exception e) {
            Thread.sleep(1000);
            token = accountService.login(authenticationRequest, request);
        }

//        authenticate(authenticationRequest.getUserName(), authenticationRequest.getPassword());
//
//        final UserDetails userDetails = accountService
//                .loadUserByUserName(authenticationRequest.getUserName());
//
//        final String token = jwtTokenUtil.generateToken(userDetails);
//
        accountService.createSession(SecurityContextHolder.getContext().getAuthentication(), request, "Bearer " + token.getSessionToken());
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(auth.getPrincipal(), token, auth.getAuthorities()));
//
//        return ResponseEntity.ok(new JwtResponse(token, expired));
        return ResponseEntity.ok(token);
    }

    private void authenticate(String username, String password) throws Exception {
//        try {
        SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password)));
//        } catch (DisabledException e) {
//            throw new Exception("USER_DISABLED", e);
//        } catch (BadCredentialsException e) {
//            throw new Exception("INVALID_CREDENTIALS", e);
//        }
    }

    @RequestMapping(value = "/api/refresh-token", method = RequestMethod.POST)
    public ResponseEntity<SessionDto> refreshToken(@RequestBody AuthUserRefreshTokenDto dto, HttpServletRequest request) {
        return ResponseEntity.ok(accountService.refreshToken(dto, request));
    }
}