package uz.uzcard.genesis.uitls;

import org.hibernate.query.Query;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.uzcard.genesis.config.Constants;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.hibernate.entity._UserSession;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Virus on 19-Sep-16.
 */
public class SessionContext extends SessionUtils {

    public static String GET_USER_QUERY = "select t from _User t where t.userName = :userName";
    public static String USER_QUERY = "select us.user from _UserSession us where us.token=:sessionId and us.expired = false";
    public static String USER_SESSION_QUERY = "select us from _UserSession us where us.token=:sessionId and us.expired = false";
    public static String ROLE_QUERY = "select r.code as code from _User t join t.roles r where t.id = :user and t.state <> :deleted";
    public static String TEAM_QUERY = "select tm.id as code from _User t join t.department d join d.teams tm where t.id = :user and t.state <> :deleted";
    //    public static String TEAM_QUERY = "select t.id as code from _Team t join t.teamLeader d join _User u on d = u.department where u.id = :user and u.state <> :deleted and t.state <> :deleted";
    private final List<String> roles = new ArrayList<>(10);
    private final List<Long> teams = new ArrayList<>(10);
    private List<String> permissions = new ArrayList<>();
    private long startDate;
    private String sessionId;
    private String language = "uz";
    private _User user;

    @Override
    public long getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        this.user = null;
        this.roles.clear();
        this.permissions.clear();
        teams.clear();
    }

    @Override
    public boolean isLoggedIn() {
        return sessionId != null && sessionId.length() > 0;
    }

    @Override
    public String getLanguage() {
        return this.language;
    }

    @Override
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public _User getUser() {
        if (user != null) {
            return user;
        }
        try {
            user = getUserBySession();
            if (user == null && SecurityContextHolder.getContext().getAuthentication() != null
                    && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null &&
                    SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof _User) {
                user = (_User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            }
            if (user == null && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String) {
                user = getUserByName((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            }

            if (user == null && sessionId != null && !sessionId.contains("(EXPIRED)")) {//Case when session is expired
                sessionId = sessionId + "(EXPIRED)";
            }
            return user;
        } catch (EntityNotFoundException exception) {
            return null;
        }
    }

    private _User getUserByName(String userName) {
        org.hibernate.Session session;
        if (getSessionFactory().isClosed()) {
            session = getSessionFactory().openSession();
        } else {
            session = getSessionFactory().getCurrentSession();
        }
        if (session.isOpen()) {
            Query queryObject = session.createQuery(GET_USER_QUERY, _User.class);
            queryObject.setParameter("userName", userName);
            queryObject.setMaxResults(1);
            queryObject.setCacheable(true);
            queryObject.setCacheRegion("query.user_session");
            _User object = (_User) queryObject.uniqueResult();
            return object;
        }
        return null;
    }

    @Override
    public List<String> getRoles() {
        if (!roles.isEmpty())
            return roles;

        return getRoleByUser();
    }

    @Override
    public List<Long> getTeams() {
        if (!teams.isEmpty())
            return teams;

        return getTeamsByUser();
    }

    private List<Long> getTeamsByUser() {
        if (getUser() == null)
            return Collections.emptyList();

        org.hibernate.Session session;
        if (getSessionFactory().isClosed()) {
            session = getSessionFactory().openSession();
        } else {
            session = getSessionFactory().getCurrentSession();
        }
        if (session.isOpen()) {
            Query<Long> queryObject = session.createQuery(TEAM_QUERY, Long.class);
            queryObject.setParameter("user", user.getId());
            queryObject.setCacheable(true);
            queryObject.setCacheRegion(Constants.Cache.QUERY_ROLE);
            queryObject.setParameter("deleted", _State.DELETED);
            teams.addAll(queryObject.list());
            return teams;
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getPermissions() {
        if (!permissions.isEmpty()) {
            return permissions;
        }
        return getMyPermissions();
    }

    private List<String> getMyPermissions() {
        if (!isLoggedIn())
            return null;

//        RolePermissionDao rolePermissionDao = ApplicationContextProvider.applicationContext.getBean(RolePermissionDao.class);
//        permissions.put(module, rolePermissionDao.getMyPermission(module));
        permissions = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).collect(Collectors.toList());
        return permissions;
    }

    public _User getUserBySession() {
        if (!isLoggedIn()) {
            return null;
        }

        org.hibernate.Session session;
        if (getSessionFactory().isClosed()) {
            session = getSessionFactory().openSession();
        } else {
            session = getSessionFactory().getCurrentSession();
        }
        if (session.isOpen()) {
            Query<_User> queryObject = session.createQuery(USER_QUERY, _User.class);
            queryObject.setParameter("sessionId", sessionId);
            queryObject.setMaxResults(1);
            queryObject.setCacheable(true);
            queryObject.setCacheRegion(Constants.Cache.QUERY_USER);
            _User object = queryObject.uniqueResult();
            return object;
        }
        return null;
    }

    private List<String> getRoleByUser() {
        if (getUser() == null)
            return Collections.emptyList();

        org.hibernate.Session session;
        if (getSessionFactory().isClosed()) {
            session = getSessionFactory().openSession();
        } else {
            session = getSessionFactory().getCurrentSession();
        }
        if (session.isOpen()) {
            Query<String> queryObject = session.createQuery(ROLE_QUERY, String.class);
            queryObject.setParameter("user", user.getId());
            queryObject.setCacheable(true);
            queryObject.setCacheRegion(Constants.Cache.QUERY_ROLE);
            queryObject.setParameter("deleted", _State.DELETED);
            roles.addAll(queryObject.list());
            return roles;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean activated() {
        _User user = getUser();
        if (user == null) return false;
        return user.isActived();
    }

    @Override
    public Long getUserId() {
        return getUser() == null ? null : user.getId();
    }

    @Override
    public String getIpAddress() {
        if (!isLoggedIn()) {
            return null;
        }
        _UserSession userSession = getUserSession();
        if (userSession == null)
            return null;

        return userSession.getIPAddress();
    }

    public _UserSession getUserSession() {
        org.hibernate.Session session;
        if (getSessionFactory().isClosed()) {
            session = getSessionFactory().openSession();
        } else {
            session = getSessionFactory().getCurrentSession();
        }
        if (session.isOpen()) {
            Query queryObject = session.createQuery(USER_SESSION_QUERY, _UserSession.class);
            queryObject.setParameter("sessionId", sessionId);
            queryObject.setMaxResults(1);
            queryObject.setCacheable(true);
            queryObject.setCacheRegion("query.user_session");
            _UserSession object = (_UserSession) queryObject.uniqueResult();
            return object;
        }
        return null;
    }
}