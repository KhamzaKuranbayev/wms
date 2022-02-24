package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.document.DateTools;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.RentDao;
import uz.uzcard.genesis.hibernate.entity.*;

import java.util.Arrays;
import java.util.Date;

import static uz.uzcard.genesis.uitls.StateConstants.*;

@Component(value = "rentDao")
public class RentDaoImpl extends DaoImpl<_Rent> implements RentDao {
    public RentDaoImpl() {
        super(_Rent.class);
    }

    @Override
    public PageStream<_Rent> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("departmentId")) {
            searchFilter.and("department.id", filter.getString("departmentId"));
        }
        if (filter.has("productItemId")) {
            searchFilter.and("productItem.id", filter.getString("productItemId"));
        }
        if (filter.has("productId")) {
            searchFilter.and("productItem.product.id", filter.getString("productId"));
        }

        if (filter.has("fromDate") && filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("expireDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("fromDate")) {
            searchFilter.and(String.format("expireDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("expireDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }

        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        if (filter.has("withReturned")) {
            searchFilter.and("-state", _State.DELETED);
        } else {
            searchFilter.and(String.format("-state : ( %s )",
                    String.join(", ", stringCollection(Arrays.asList(DELETED, RETURNED)))));
        }

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Rent.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
//        fullTextQuery.setSort(new Sort(new SortField("id", SortField.Type.STRING, true)));
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Rent>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _Rent getByProductIdAndDepartment(_ProductItem productItem, _Department department) {
        return (_Rent) findSingle("select t from _Rent t where t.state not in (:deleted) and t.productItem = :productItem and t.department = :department",
                preparing(new Entry("productItem", productItem), new Entry("department", department), new Entry("deleted", Arrays.asList(_State.DELETED, _State.RETURNED))));
    }
}
