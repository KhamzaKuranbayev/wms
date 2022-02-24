package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.hibernate.search.query.facet.Facet;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ContractDao;
import uz.uzcard.genesis.hibernate.entity._Contract;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.enums.SupplyType;
import uz.uzcard.genesis.hibernate.enums.UserAgreementStatusType;

import java.util.*;

import static uz.uzcard.genesis.uitls.StateConstants.*;

@Component(value = "contractDao")
public class ContractDaoImpl extends DaoImpl<_Contract> implements ContractDao {
    public ContractDaoImpl() {
        super(_Contract.class);
    }

    @Override
    public PageStream<_Contract> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();

        if (filter.has("contractId"))
            searchFilter.and("id", filter.getString("contractId"));
        if (filter.has("contractCode")) {
            if (filter.getString("contractCode").equals(QueryParser.escape(filter.getString("contractCode")))) {
                searchFilter.and("code", filter.getString("contractCode").trim() + "*");
            } else {
                searchFilter.and("code", QueryParser.escape(filter.getString("contractCode").trim()) + "");
            }
        }
        if (filter.has("codeSearch"))
            searchFilter.and("code", "\"" + QueryParser.escape(filter.getString("codeSearch").trim()) + "*" + "\"");
        if (filter.has("groupId"))
            searchFilter.and("items.productGroup.id", filter.getString("groupId"));
        if (filter.has("typeId"))
            searchFilter.and("items.productType.id", filter.getString("typeId"));
        if (filter.has("productName"))
            searchFilter.and("items.product.name", filter.getString("productName").trim() + "*");
        if (filter.has("supplierId"))
            searchFilter.and("supplier.id", filter.getString("supplierId"));
        if (filter.has("supplierType"))
            searchFilter.and("supplyType", "" + SupplyType.valueOf(filter.getString("supplierType")));
        if (filter.has("contractStatus"))
            searchFilter.and("state", filter.getString("contractStatus"));
        if (filter.has("updatedBy"))
            searchFilter.and("updatedByUser.id", filter.getString("updatedBy"));
        if (filter.getBool("forInitiator")) {
//            searchFilter.and("items.userAgreements.user.id", SessionUtils.getInstance().getUserId().toString());
//            searchFilter.and("items.userAgreements.notificationSent:true");
//            searchFilter.and("items.userAgreements.statusType", UserAgreementStatusType.WAITING.name());
            searchFilter.and(String.format("items.userAgreements.initiatorInfo:%s_%s_%s", getUser().getId(), true, UserAgreementStatusType.WAITING.name()));
            filter.setSortColumn("auditInfo.updatedDate");
            filter.setSortType(false);
        }

        if (filter.has("fromDate") && filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("conclusionDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("fromDate")) {
            searchFilter.and(String.format("conclusionDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.DAY),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.DAY)));
        } else if (filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("conclusionDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }
        if (filter.has("orderNumber")) {
            searchFilter.and("items.orderItems.parent.numb", filter.getString("orderNumber"));
        }
        if (filter.has("order.item.status")) {
            searchFilter.and("items.orderItems.state", filter.getString("order.item.status"));
        }
        if (filter.getBool("forPrintQrCode")) {
            searchFilter.and(String.format("items.state : ( %s )",
                    String.join(", ", stringCollection(Arrays.asList(CONTRACT_ITEM_ACCEPTED, CONTRACT_ITEM_PART_ACCEPTED, CONTRACT_ITEM_PARTITION_ACCEPTED)))));
        }

        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
//        if (filter.has("state"))
//            searchFilter.and("items.state", filter.getString("state"));

        if (filter.has("isMakeContract"))
            searchFilter.and(String.format("-state : ( %s )", String.join(", ", stringCollection(filter.getStrings("isMakeContract")))));
        else
            searchFilter.and("-state", _State.DELETED);

        if (filter.getBool("isForMobile"))
            searchFilter.and(String.format("items.state : ( %s )", String.join(", ", stringCollection(Arrays.asList(NEW, CONTRACT_ITEM_PARTITION_ACCEPTED, CONTRACT_ITEM_PART_ACCEPTED)))));

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Contract.class);

        if (filter.getSortColumn() != null) {
            switch (filter.getSortColumn()) {
                case "code":
                    fullTextQuery.setSort(new Sort(new SortField("codeSort", SortField.Type.STRING, filter.getSortType())));
                    break;
                default:
                    fullTextQuery.setSort(new Sort(new SortField(filter.getSortColumn(), SortField.Type.STRING, filter.getSortType())));
                    break;
            }
        } else
            fullTextQuery.setSort(new Sort(new SortField("auditInfo.updatedDate", SortField.Type.STRING, true)));

        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Contract>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _Contract getDefaultByDepartment(_Department department) {
        return (_Contract) findSingle("select t from _Contract t left join t.auditInfo.createdByUser u left join u.department d " +
                        " where t.state != :deleted and d = :department and t.defaultYearly = :defYear",
                preparing(new Entry("deleted", _State.DELETED),
                        new Entry("department", department),
                        new Entry("defYear", Calendar.getInstance().get(Calendar.YEAR))));
    }

    @Override
    public boolean checkByNum(String code) {
        return ((Long) findSingle("select count(t) from _Contract t where lower(trim(t.code)) = lower(trim(:code)) and t.state != :deleted ",
                preparing(new Entry("code", code), new Entry("deleted", _State.DELETED)))) > 0;
    }

    @Override
    public boolean checkByIdWithCode(Long id, String code) {
        return ((Long) findSingle("select count(t) from _Contract t where t.id != :id and lower(trim(t.code)) = lower(trim(:code)) ",
                preparing(new Entry("id", id), new Entry("code", code)))) > 0;
    }

    @Override
    public _Contract getByCode(String contractCode) {
        return (_Contract) findSingle("select t from _Contract t where t.code = :code",
                preparing(new Entry("code", contractCode)));
    }

    @Override
    public List<Facet> getBySupplier(DashboardFilter filterRequest) {
        SearchFilter searchFilter = new SearchFilter();
        Date[] period = filterRequest.getPeriod();

        searchFilter.and(String.format("auditInfo.creationDate:[%s TO %s]",
                DateTools.dateToString(period[0], DateTools.Resolution.MILLISECOND),
                DateTools.dateToString(period[1], DateTools.Resolution.MILLISECOND)));
        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-guessReceiveDate:null AND *:*");
        String temp = String.format(" ((diffDate:{* TO 0.0]) OR (guessReceiveDate:[%s TO %s] AND -completedDate:null )) ",
                DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                DateTools.dateToString(new Date(), DateTools.Resolution.MILLISECOND));
        searchFilter.and(temp);

        List<Facet> contractByFacetSupplier = getFacetWithSorting(searchFilter.toString(), "supplier.nameFacet", 10000, "auditInfo.updatedDate");
        return contractByFacetSupplier;
    }

    @Override
    public List<Facet> getByContractStatus(DashboardFilter filterRequest, String status, String groupBy, String filterBy) {
        SearchFilter searchFilter = new SearchFilter();
        Date[] period = filterRequest.getPeriod();

        searchFilter.and(String.format("%s:[%s TO %s]", filterBy,
                DateTools.dateToString(period[0], DateTools.Resolution.MILLISECOND),
                DateTools.dateToString(period[1], DateTools.Resolution.MILLISECOND)));
        searchFilter.and("-state", _State.DELETED);
        if (status != null)
            searchFilter.and("state", status);

        switch (filterRequest.getResolutionType()) {
            case DAY:
                return getFacetWithSorting(searchFilter.toString(), groupBy, 10000, "conclusionDate");
            case MONTH:
                return getFacetWithSorting(searchFilter.toString(), groupBy, 10000, "conclusionDate");
        }
        return Collections.emptyList();
    }
}