package uz.uzcard.genesis.uitls;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.config.ApplicationContextProvider;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.hibernate.entity._User;

import java.util.Date;
import java.util.List;

@Component
public abstract class SessionUtils {
    private static final ThreadLocal<SessionUtils> instance = new ThreadLocal<SessionUtils>();
    private static Class serverSecurityContextClass = DefaultSessionContext.class;

    static {
        serverSecurityContextClass = SessionContext.class;
    }

    public static SessionUtils getInstance() {
        if (instance.get() == null) {
            createServerSecurityContext();
        }
        return instance.get();
    }

    private static void createServerSecurityContext() {
        try {
            SessionUtils context = (SessionUtils) serverSecurityContextClass.getConstructor().newInstance();
            instance.set(context);
            context.setStartDate(new Date().getTime());
        } catch (Throwable t) {
            throw new RpcException("ERROR_WHILE_CREATING_SESSION_UTILS");
        }
    }

    public abstract long getStartDate();

    public abstract void setStartDate(long startDate);

    public abstract String getSessionId();

    public abstract void setSessionId(String sessionId);

    public abstract boolean isLoggedIn();

    public SessionFactory getSessionFactory() {
        return (SessionFactory) ApplicationContextProvider.applicationContext.getBean("sessionFactory");
    }

    public abstract String getLanguage();

    public abstract void setLanguage(String language);

    public abstract _User getUser();

    public abstract List<String> getRoles();

    public abstract List<Long> getTeams();

    public abstract List<String> getPermissions();

    public abstract boolean activated();

    public abstract Long getUserId();

    public abstract String getIpAddress();
}