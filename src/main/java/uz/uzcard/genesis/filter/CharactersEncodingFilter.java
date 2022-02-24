package uz.uzcard.genesis.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Virus on 9/28/2016.
 */
@WebFilter(filterName = "charsetEncodingFilter", asyncSupported = true,
        initParams = {
                @WebInitParam(name = "encoding", value = "UTF-8"),
                @WebInitParam(name = "forceEncoding", value = "true"),
        }, urlPatterns = "/*")
//@Async
public class CharactersEncodingFilter extends org.springframework.web.filter.CharacterEncodingFilter {
    public static String exclude = ".*\\.(jpg|svg|eot|jpeg|png|ico|gif|css|js|woff|woff2|ttf|ico|html|cache.html|cache.js|nocache.js).*";

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String url = request.getRequestURI().toLowerCase();

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (url.matches(exclude)) {
            Date now = new Date();
            HttpServletResponse httpResponse = response;
            httpResponse.setDateHeader("Date", now.getTime());
            // one day old
            httpResponse.setDateHeader("Expires", now.getTime() + 3600000L);
            if (url.startsWith("/api/attachment")) {
                httpResponse.setHeader("Pragma", "");
            } else
                httpResponse.setHeader("Pragma", "no-cache");
//            httpResponse.setHeader("Cache-control", "cache, store, must-revalidate");
            httpResponse.setHeader("Cache-Control", "max-age=7776000, public");

            filterChain.doFilter(request, response);
        } else {
//            System.out.println(url);
//            filterChain.doFilter(request, response);
            super.doFilterInternal(request, response, filterChain);
        }
    }

}
