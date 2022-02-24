package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.UserDao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component(value = "userDao")
public class UserDaoImpl extends DaoImpl<_User> implements UserDao {
    public UserDaoImpl() {
        super(_User.class);
    }

    @Override
    public Stream<_User> list(FilterParameters filter) {
        CustomFilter customFilter = new CustomFilter(filter).invoke();
        String filterQuery = customFilter.getFilterQuery();
        Map<String, Object> params = customFilter.getParams();
        return findInterval("select distinct t from _User t left join t.roles r left join t.department d " +
                " where t.state <> 'DELETED' " + filterQuery +
                " order by t.id", params, filter.getStart(), filter.getSize());
    }

    @Override
    public _User getByUseName(String userName) {

        return (_User) findSingle("select t from _User t where t.state <> :deleted and t.userName = :userName ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("userName", userName)));
    }

    @Override
    public _User getByPhoneNumber(String phoneNumber) {
        return (_User) findSingle("select t from _User t where t.state <> :deleted and t.phone = :phone ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("phone", phoneNumber)));
    }

    @Override
    public _User getByEmail(String email) {
        return (_User) findSingle("select t from _User t where t.state <> :deleted and t.email = :email ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("email", email)));
    }

    @Override
    public Stream<_User> findByIds(List<Long> ids) {
        return find("select t from _User t where t.state <> :deleted and t.id in (:ids)",
                preparing(new Entry("ids", ids), new Entry("deleted", _State.DELETED)));
    }

    @Override
    public _User getByUseNameWithOutId(String userName, Long id) {
        return (_User) findSingle("select t from _User t where t.state <> :deleted and t.userName = :userName and t.id <> :id ",
                preparing(new Entry("deleted", _State.DELETED),
                        new Entry("userName", userName),
                        new Entry("id", id)));
    }

    @Override
    public Integer total(FilterParameters filter) {
        CustomFilter customFilter = new CustomFilter(filter).invoke();
        String filterQuery = customFilter.getFilterQuery();
        Map<String, Object> params = customFilter.getParams();
        return ((Long) findSingle("select count(distinct t.id) from _User t left join t.roles r left join t.department d " +
                " where t.state <> 'DELETED' " +
                filterQuery, params)).intValue();
    }

    @Override
    public Stream<_User> findByDepartment(_Department department) {
        return find("select t from _User t where t.state <> :deleted and t.department = :department",
                preparing(new Entry("department", department), new Entry("deleted", _State.DELETED)));
    }

    @Override
    public Stream<_User> findByDepartmentType(OrderClassification depType) {
        return find("select t from _User t where t.state <> :deleted and t.department.depType = :depType",
                preparing(new Entry("depType", depType), new Entry("deleted", _State.DELETED)));
    }

    @Override
    public Stream<_User> findByRole(String code) {
        return find("select t from _User t where t.state <> :deleted and t.role.code = :code",
                preparing(new Entry("code", code), new Entry("deleted", _State.DELETED)));
    }

    @Override
    public Stream<_User> findAllWithoutId(Long withOutId) {
        return find("select t from _User t where t.state <> :deleted and t.id != :withOutId",
                preparing(new Entry("withOutId", withOutId), new Entry("deleted", _State.DELETED)));
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

        public CustomFilter invoke() {
            filterQuery = "";
            params = preparing();
            if (filter.has("roles")) {
                filterQuery += " and r.code in (:roles)";
                params.put("roles", filter.getStrings("roles"));
            }
            if (filter.has("departmentId")) {
                filterQuery += " and t.department.id in (:departmentId)";
                params.put("departmentId", filter.getLong("departmentId"));
            }
            if (filter.has("isDepartment")) {
                filterQuery += " and t.department is not null ";
            }
            if (filter.has("email")) {
                filterQuery += " and t.email like :email ";
                params.put("email", "%" + filter.getString("email") + "%");
            }
            if (filter.has("phoneNumber")) {
                filterQuery += " and t.phone like :phoneNumber ";
                params.put("phoneNumber", "%" + filter.getString("phoneNumber") + "%");
            }
            if (filter.has("firstName")) {
                filterQuery += " and t.firstName like :firstName ";
                params.put("firstName", "%" + filter.getString("firstName") + "%");
            }
            if (filter.has("lastName")) {
                filterQuery += " and t.lastName like :lastName ";
                params.put("lastName", "%" + filter.getString("lastName") + "%");
            }
            if (filter.has("userName")) {
                filterQuery += " and t.userName like :userName ";
                params.put("userName", "%" + filter.getString("userName") + "%");
            }
            if (filter.has("depType")) {
                filterQuery += " and d.depType = :depType ";
                params.put("depType", OrderClassification.valueOf(filter.getString("depType")));
            }
            if (filter.has("name")) {
                filterQuery += " and (lower(t.userName) like :name or lower(t.firstName) like :name " +
                        " or lower(t.lastName) like :name or lower(t.phone) like :name or lower(t.email) like :name)";
                params.put("name", filter.getSearchQuery("name"));
            }
            return this;
        }
    }
}