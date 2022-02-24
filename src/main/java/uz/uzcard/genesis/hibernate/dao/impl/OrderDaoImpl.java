package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.hibernate.search.query.facet.Facet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.config.Constants;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.OrderDao;
import uz.uzcard.genesis.hibernate.dao.TeamDao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._Order;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.*;
import java.util.stream.Stream;

@Component(value = "orderDao")
public class OrderDaoImpl extends DaoImpl<_Order> implements OrderDao {
    @Autowired
    private TeamDao teamDao;

    public OrderDaoImpl() {
        super(_Order.class);
    }

    @Override
    public PageStream<_Order> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("sendDateIsNotNull") && filter.getBool("sendDateIsNotNull"))
            searchFilter.and("-sendDate:null AND *:*");

        if (filter.has("order_id")) {
            searchFilter.and("id", filter.getString("order_id"));
        }
        if (filter.has("state")) {
            searchFilter.and("state:( " + filter.getString("state") + " )");
        }
        if (filter.has("orderNumber")) {
            searchFilter.and("numb", filter.getString("orderNumber").trim() + "*");
        }
        if (filter.has("groupId")) {
            searchFilter.and("items.productGroup.id", filter.getString("groupId"));
        }
        if (filter.has("typeId")) {
            searchFilter.and("items.productType.id", filter.getString("typeId"));
        }
        if (filter.has("name")) {
            searchFilter.and("items.product.name", filter.getString("name").trim() + "*");
        }
        if (filter.has("positionState")) {
            searchFilter.and("items.state", filter.getString("positionState"));
        }
        if (filter.has("itemStatuses")) {
            if (filter.getBool("fromOzl")) {
                SearchFilter temp = new SearchFilter();
                temp.and("items.state:( " + String.join(", ", stringCollection(filter.getStrings("itemStatuses"))) + " )");
                temp.or("( items.state:" + _State.YES_PRODUCT + " AND -items.contractItem:null )");
                searchFilter.and(String.format("( %s )", temp.toString()));
            } else
                searchFilter.and(String.format("items.state : ( %s )", String.join(", ", stringCollection(filter.getStrings("itemStatuses")))));
        }
        if (filter.has("requestState")) {
            searchFilter.and("state", filter.getString("requestState"));
        }
        if (filter.has("contractNumber")) {
            searchFilter.and("items.contractItem.parent.code", filter.getString("contractNumber").trim() + "*");
        }
        if (filter.has("supplierId")) {
            searchFilter.and("items.contractItem.parent.supplier.id", filter.getString("supplierId"));
        }
        if (filter.has("isHasContract")) {
            searchFilter.and("-items.contractItem:null");
        }
        if (filter.has("owner")) {
            searchFilter.and("auditInfo.createdByUser.id", filter.getString("owner"));
        }
        if (filter.has("initiator")) {
            searchFilter.and("auditInfo.createdByUser.id", filter.getString("initiator"));
        }
        if (filter.has("departmentId")) {
            searchFilter.and("department.id", filter.getString("departmentId"));
        }

        if (filter.has("fromDate") && filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("sendDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("fromDate")) {
            searchFilter.and(String.format("sendDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("sendDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }
        if (filter.has("statusChangedUser")) {
            searchFilter.and("items.statusChangedUser.id", filter.getString("statusChangedUser"));
        }
        if (filter.has("teams"))
            searchFilter.and(String.format("teams:( %s )", String.join(", ", stringCollection(filter.getStrings("teams")))));
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
//        SearchFilter temp = new SearchFilter();
//        temp.and("department.id", "" + getUser().getDepartment().getId());
//        temp.or("department.parent.id", "" + getUser().getDepartment().getId());
//        searchFilter.and(String.format("( %s )", temp.toString()));

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Order.class);
        if (filter.getSortColumn() != null)
            switch (filter.getSortColumn()) {
                case "id":
                    fullTextQuery.setSort(new Sort(new SortField("auditInfo.creationDate", SortField.Type.STRING, true)));
                    break;
                case "numb":
                    fullTextQuery.setSort(new Sort(new SortField("orderNumbSort", SortField.Type.INT, !filter.getSortType())));
                    break;
                case "sendDate":
                    fullTextQuery.setSort(new Sort(new SortField("sendDate", SortField.Type.STRING, !filter.getSortType())));
                    break;
                case "updateUser":
                    fullTextQuery.setSort(new Sort(new SortField("updateUser", SortField.Type.STRING, !filter.getSortType())));
                    break;
                default:
                    fullTextQuery.setSort(new Sort(new SortField("timeToBeEntered", SortField.Type.STRING, !filter.getSortType())));
            }
        else
            fullTextQuery.setSort(new Sort(new SortField("timeToBeEntered", SortField.Type.STRING, !filter.getSortType())));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Order>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public Stream<_Order> list(FilterParameters filter) {
        CustomFilter customFilter = new CustomFilter(filter).invoke();
        String query = customFilter.getFilterQuery();
        Map<String, Object> params = customFilter.getParams();
        return findInterval("select t from _Order t where t.state != 'DELETED' " + query + " order by t.id desc ",
                params, filter.getStart(), filter.getSize());
    }

    @Override
    public Integer total(FilterParameters filter) {
        CustomFilter customFilter = new CustomFilter(filter).invoke();
        String query = customFilter.getFilterQuery();
        Map<String, Object> params = customFilter.getParams();
        return ((Long) findSingle("select count(t) from _Order t where t.state != 'DELETED' " + query, params)).intValue();
    }

    @Override
    public _Order save(_Order entity) {
        if (entity.isNew() && entity.getNumb() < 2) {
            int maxNumber = getMaxNumber();
            entity.setNumb(maxNumber + 1);
            entity.setTeams(SessionUtils.getInstance().getTeams());
        } else
            entity.setTeams(teamDao.findIdsByUser(entity.getAuditInfo().getCreatedByUser()));
        return super.save(entity);
    }

    private int getMaxNumber() {
        Integer numb = (Integer) findSingle("select max(t.numb) from _Order t where t.state <> :deleted",
                preparing(new Entry("deleted", _State.DELETED)));
        if (numb == null || numb == 0) return 1;
        return numb.intValue();
    }

    @Override
    public _Order getDefaultByDepartment(_Department department) {
        if (department == null)
            return null;
        return (_Order) findSingle("select t from _Order t left join t.department d " +
                        " where t.state != :deleted and t.defaultYearly = :defYear and d = :dep",
                preparing(new Entry("deleted", _State.DELETED), new Entry("defYear", Calendar.getInstance().get(Calendar.YEAR)),
                        new Entry("dep", department)));
    }

    @Override
    public _Order getByNumber(Integer numb) {
        return (_Order) findSingle("select t from _Order t where t.numb = :numb",
                preparing(new Entry("numb", numb)), Constants.Cache.QUERY_ORDER);
    }

    @Override
    public List<Facet> getAllSendOrder(DashboardFilter filterRequest) {
        SearchFilter searchFilter = new SearchFilter();
        Date[] period = filterRequest.getPeriod();
        searchFilter.and(String.format("sendDate:[%s TO %s]",
                DateTools.dateToString(period[0], DateTools.Resolution.MILLISECOND),
                DateTools.dateToString(period[1], DateTools.Resolution.MILLISECOND)));

        switch (filterRequest.getResolutionType()) {
            case DAY:
                return getFacetWithSorting(searchFilter.toString(), "sendDateFacetDay", 10000, "sendDate");
            case MONTH:
                return getFacetWithSorting(searchFilter.toString(), "sendDateFacetMonth", 10000, "sendDate");
        }
        return Collections.emptyList();
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
            if (filter.has("sendDateIsNotNull")) {
                filterQuery = " and t.sendDate is not null";
            }
            return this;
        }
    }
}