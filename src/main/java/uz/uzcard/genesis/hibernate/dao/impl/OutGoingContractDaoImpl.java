package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.OutGoingContractDao;
import uz.uzcard.genesis.hibernate.entity._OutGoingContract;
import uz.uzcard.genesis.hibernate.entity._State;

@Component(value = "outgoingDao")
public class OutGoingContractDaoImpl extends DaoImpl<_OutGoingContract> implements OutGoingContractDao {
    public OutGoingContractDaoImpl() {
        super(_OutGoingContract.class);
    }

    @Override
    public PageStream<_OutGoingContract> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("contractNumber"))
            searchFilter.and("contractNumber", filter.getString("contractNumber"));
        if (filter.has("supplierId"))
            searchFilter.and("supplier.id", filter.getString("supplierId"));
        if (filter.has("productId"))
            searchFilter.and("product.id", filter.getString("productId"));
        if (filter.has("customerId"))
            searchFilter.and("customer.id", filter.getString("customerId"));
        if (filter.has("requestCount"))
            searchFilter.and("requestCount", filter.getString("requestCount"));
        if (filter.has("contractBalance"))
            searchFilter.and("contractBalance", filter.getString("contractBalance"));
        if (filter.has("isCompleted"))
            searchFilter.and("state", "COMPLETED");
        if (filter.has("isNotCompleted"))
            searchFilter.and("state", "CONCLUDED");

        // closeContractDate
        if (filter.has("fromCloseContractDate") && filter.has("toCloseContractDate")) {
            java.util.Date toDate = new DateTime(filter.getDate("toCloseContractDate")).toDate();
            searchFilter.and(String.format("closeContractDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromCloseContractDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("fromCloseContractDate")) {
            searchFilter.and(String.format("closeContractDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromCloseContractDate"), DateTools.Resolution.DAY),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.DAY)));
        } else if (filter.has("toCloseContractDate")) {
            java.util.Date toDate = new DateTime(filter.getDate("toCloseContractDate")).toDate();
            searchFilter.and(String.format("closeContractDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }

        // closeDate
        if (filter.has("fromCloseDate") && filter.has("toCloseDate")) {
            java.util.Date toDate = new DateTime(filter.getDate("toCloseDate")).toDate();
            searchFilter.and(String.format("closeDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromCloseDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("fromCloseDate")) {
            searchFilter.and(String.format("closeDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromCloseDate"), DateTools.Resolution.DAY),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.DAY)));
        } else if (filter.has("toCloseDate")) {
            java.util.Date toDate = new DateTime(filter.getDate("toCloseDate")).toDate();
            searchFilter.and(String.format("closeDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }

        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _OutGoingContract.class);
        if (filter.getSortColumn() != null)
            fullTextQuery.setSort(new Sort(new SortField(filter.getSortColumn(), SortField.Type.STRING, filter.getSortType())));
        else
            fullTextQuery.setSort(new Sort(new SortField("id", SortField.Type.STRING, true)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_OutGoingContract>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public Boolean checkByContractNumber(String contractNumber) {
        return ((Long) findSingle("select count(t) from _Contract t where lower(trim(t.code)) = lower(trim(:code)) ",
                preparing(new Entry("code", contractNumber)))) > 0;
    }

    @Override
    public _OutGoingContract getByContractNumber(String contractNumber) {
        return (_OutGoingContract) findSingle("select t from _OutGoingContract t where t.state <> :deleted and t.contractNumber = :code ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("code", contractNumber)));
    }
}
