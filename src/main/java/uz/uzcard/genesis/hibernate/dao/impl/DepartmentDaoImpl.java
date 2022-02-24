package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.config.Constants;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.DepartmentDao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._Warehouse;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component(value = "departmentDao")
public class DepartmentDaoImpl extends DaoImpl<_Department> implements DepartmentDao {
    public DepartmentDaoImpl() {
        super(_Department.class);
    }

    @Override
    public _Department getOneByType(OrderClassification type) {
        return (_Department) findSingle("select t from _Department t where t.state <> :deleted and t.depType = :type"
                , preparing(new Entry("deleted", _State.DELETED), new Entry("type", type)));
    }

    @Override
    public Stream<_Department> getDepartmentByIds(List<Long> ids) {
        return find("select distinct t from _Department t where t.state <> :deleted and t.id in (:ids)",
                preparing(new Entry("ids", ids), new Entry("deleted", _State.DELETED)));
    }

    @Override
    public List<Long> findChildAndMe(_Department department) {
        ArrayList<Long> list = new ArrayList<>();
        list.add(department.getId());
        list.addAll((List<Long>) find("select t.id from _Department t join t.parent p where (p = :parent or t = :department) and t.state != :deleted and p.state != :deleted",
                preparing(new Entry("parent", department), new Entry("department", department), new Entry("deleted", _State.DELETED)),
                Constants.Cache.QUERY_DEPARTMENT));
        return list;
    }

    @Override
    public List<Long> getByAllTeamsByDepartments(List<Long> teamIds) {
        return (List<Long>) find("select distinct d.id from _Department d left join d.teams t where t.id in (:teamIds) and t.state != :deleted and d.state != :deleted",
                preparing(new Entry("teamIds", teamIds), new Entry("deleted", _State.DELETED)), Constants.Cache.QUERY_DEPARTMENT);
    }

    @Override
    public Stream<_Department> findAll() {
        return find("select t from _Department t where t.state != :deleted ",
                preparing(new Entry("deleted", _State.DELETED)));
    }

    @Override
    public Stream<_Department> findDepartmentByWarehouse(_Warehouse warehouse) {
        return find("select t from _Department t left join t.warehouses w where t.state != :deleted and w = :warehouse",
                preparing(new Entry("deleted", _State.DELETED), new Entry("warehouse", warehouse)));
    }

    //    @Override
//    public Stream<_Department> getDepartmentByTeams(List<Long> teamIds) {
//        return find("select distinct t from _Department t " +
//                        "left join t.teams tm where t.state <> :deleted and tm.id in (:teamIds)",
//                preparing(new Entry("teamIds", teamIds), new Entry("deleted", _State.DELETED)));
//    }
    @Override
    public Stream<_Department> list(FilterParameters filter) {
        CustomFilter customFilter = new CustomFilter(filter).invoke();
        String filterQuery = customFilter.getFilterQuery();
        Map<String, Object> params = customFilter.getParams();
        return findInterval("select distinct t from _Department t" +
                " left join t.teams tm" +
                " where t.state <> 'DELETED' " + filterQuery +
                " order by t.id", params, filter.getStart(), filter.getSize());
    }

    @Override
    public Integer total(FilterParameters filter) {
        CustomFilter customFilter = new CustomFilter(filter).invoke();
        String query = customFilter.getFilterQuery();
        Map<String, Object> params = customFilter.getParams();
        return ((Long) findSingle("select count(t) from _Department t" +
                " left join t.teams tm" +
                " where t.state <> 'DELETED' " + query, params)).intValue();
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
            if (filter.has("teamIds")) {
                filterQuery += " and tm.id in (:teamIds)";
                params.put("teamIds", filter.getLongs("teamIds"));
            }
            if (filter.has("parentId")) {
                filterQuery += " and t.parent.id = :parentId";
                params.put("parentId", filter.getLong("departmentId"));
            }
            if (filter.has("name")) {
                filterQuery += " and ((lower(t.nameEn) like :name) or (lower(t.nameUz) like :name) or (lower(t.nameRu) like :name))";
                params.put("name", "%" + filter.getSearchQuery("name"));
            }
            return this;
        }
    }
}
