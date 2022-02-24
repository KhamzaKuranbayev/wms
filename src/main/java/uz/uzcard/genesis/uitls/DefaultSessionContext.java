package uz.uzcard.genesis.uitls;


import uz.uzcard.genesis.hibernate.entity._User;

import java.util.Collections;
import java.util.List;

/**
 * Created by Virus on 2016/12/02.
 */
public class DefaultSessionContext extends SessionUtils {

    private long startDate;
    private String sessionId;

    @Override
    public long getStartDate() {
        return 0;
    }

    @Override
    public void setStartDate(long startDate) {
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public void setSessionId(String sessionId) {

    }

    @Override
    public boolean isLoggedIn() {
        return false;
    }

    @Override
    public String getLanguage() {
        return "en";
    }

    @Override
    public void setLanguage(String language) {
    }

    @Override
    public _User getUser() {
        return null;
    }

    @Override
    public List<String> getRoles() {
        return Collections.emptyList();
    }

    @Override
    public List<Long> getTeams() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getPermissions() {
        return null;
    }

    @Override
    public boolean activated() {
        return false;
    }

    @Override
    public Long getUserId() {
        return null;
    }

    @Override
    public String getIpAddress() {
        return null;
    }
}