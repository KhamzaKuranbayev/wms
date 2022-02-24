package uz.uzcard.genesis.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import uz.uzcard.genesis.config.ApplicationContextProvider;
import uz.uzcard.genesis.uitls.SessionUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

@WebFilter(filterName = "logAuditFilter", urlPatterns = "/*", asyncSupported = true)
public class LogAuditFilter extends HttpServlet implements Filter {
    private static final Logger log = LogManager.getLogger(LogAuditFilter.class);
    private SessionLocaleResolver localeResolver;

    public SessionLocaleResolver getLocaleResolver() {
        if (localeResolver == null)
            localeResolver = ApplicationContextProvider.applicationContext.getBean(SessionLocaleResolver.class);
        return localeResolver;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Long start = System.currentTimeMillis();
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        SessionUtils.getInstance().setSessionId(request.getHeader("authorization"));
        String url = request.getRequestURI().toLowerCase();
        if (url.matches(CharactersEncodingFilter.exclude)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String lang = request.getHeader("lang");
        if (lang != null) {
            SessionUtils.getInstance().setLanguage(lang);
        }
        if (StringUtils.isEmpty(SessionUtils.getInstance().getLanguage()))
            SessionUtils.getInstance().setLanguage("uz");
        getLocaleResolver().setLocale(request, response, Locale.forLanguageTag(SessionUtils.getInstance().getLanguage()));
        request.setAttribute("lang", SessionUtils.getInstance().getLanguage());

        filterChain.doFilter(servletRequest, servletResponse);

        long finish = System.currentTimeMillis();
        log.info("->->Request = [ " + request.getRequestURI() + " ] Elapsed time to proceed this request = " + (finish - start));
    }
}