package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.config.Constants;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.TeamDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._Team;
import uz.uzcard.genesis.hibernate.entity._User;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component(value = "teamDao")
public class TeamDaoImpl extends DaoImpl<_Team> implements TeamDao {

    public TeamDaoImpl() {
        super(_Team.class);
    }


    @Override
    public Stream<_Team> list(FilterParameters filter) {
        TeamDaoImpl.CustomFilter customFilter = new TeamDaoImpl.CustomFilter(filter).invoke();
        String filterQuery = customFilter.getFilterQuery();
        Map<String, Object> params = customFilter.getParams();
        return findInterval("select distinct t from _Team t " +
                " where t.state <> 'DELETED' " + filterQuery +
                " order by t.id", params, filter.getStart(), filter.getSize());
    }

    @Override
    public List<Long> findIdsByUser(_User user) {
        return (List<Long>) find("select tm.id as code from _User t join t.department d join d.teams tm where t = :user and t.state <> :deleted",
                preparing(new Entry("user", user), new Entry("deleted", _State.DELETED)), Constants.Cache.QUERY_TEAM);
    }

    @Override
    public List<Long> getMyChildTeamIds() {
        return (List<Long>) find("select t.id from _Team t join t.teamLeader d join _User u on u.department = d " +
                        " where t.state != :deleted and d.state != :deleted and u = :user",
                preparing(new Entry("deleted", _State.DELETED), new Entry("user", getUser())),
                Constants.Cache.QUERY_TEAM);
    }

    private class CustomFilter {
        private final FilterParameters filter;
        private String filterQuery;
        private Map<String, Object> params;

        public CustomFilter(FilterParameters filter) {
            this.filter = filter;
        }

        public String getFilterQuery() {
            return filterQuery;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public TeamDaoImpl.CustomFilter invoke() {
            filterQuery = "";
            params = preparing();
            if (filter.has("id")) {
                filterQuery += " and t.id = :id ";
                params.put("id", filter.getLong("id"));
            }
            if (filter.has("name")) {
                filterQuery += " and t.name like :email ";
                params.put("email", "%" + filter.getString("name") + "%");
            }
            /*if (filter.has("departments")) {
                filterQuery += " and t.name like :email ";
                params.put("email", "%" + filter.getString("name") + "%");
            }*/
            return this;
        }
    }
}
