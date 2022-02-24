/*
package uz.uzcard.genesis.filter;

import io.jsonwebtoken.ExpiredJwtException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzcard.genesis.jwt.JwtTokenUtil;
import uz.uzcard.genesis.service.AccountService;
import uz.uzcard.genesis.uitls.SessionUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(JwtRequestFilter.class);
    @Autowired
    private AccountService accountService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Long start = System.currentTimeMillis();

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get
        // only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            SessionUtils.getInstance().setSessionId(jwtToken);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            } catch (Exception e) {
                System.out.println("Unable to get JWT Token");
            }
        } else if (!"/api/authenticate".equals(request.getRequestURI())) {
//            logger.warn(request.getRequestURI() + " => JWT Token does not begin with Bearer String");
            //todo vaqtincha
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
//                SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth.getCredentials() != null) {
                    SessionUtils.getInstance().setSessionId("" + auth.getCredentials());
                }
            }
        }

        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.accountService.loadUserByUserName(username);

            // if token is valid configure Spring Security to manually set
            // authentication
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } else {
                //todo vaqtincha
                if (SecurityContextHolder.getContext().getAuthentication() != null)
                    SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
            }
        }

        chain.doFilter(request, response);

        long finish = System.currentTimeMillis();
        if (!request.getRequestURI().matches(CharactersEncodingFilter.exclude)) {
            log.info("->->Request = [ " + request.getMethod() + " : " + request.getRequestURI() + " ] Elapsed time to proceed this request = " + (finish - start));
        }
    }

}*/
