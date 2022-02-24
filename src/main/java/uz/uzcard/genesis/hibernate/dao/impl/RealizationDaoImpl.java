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
import uz.uzcard.genesis.hibernate.dao.RealizationDao;
import uz.uzcard.genesis.hibernate.entity._Realization;
import uz.uzcard.genesis.hibernate.entity._State;

import java.util.Date;

@Component(value = "realizationDao")
public class RealizationDaoImpl extends DaoImpl<_Realization> implements RealizationDao {

    public RealizationDaoImpl() {
        super(_Realization.class);
    }

    @Override
    public PageStream<_Realization> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();

        if (filter.has("id")) {
            searchFilter.and("id", filter.getString("id"));
        }
        if (filter.has("contractNumber")) {
            searchFilter.and("contractNumber", filter.getString("contractNumber").trim() + "*");
        }
        if (filter.has("realizatorId")) {
            searchFilter.and("realizator.id", filter.getString("realizatorId"));
        }
        if (filter.has("departmentId")) {
            searchFilter.and("realizator.department.id", filter.getString("departmentId"));
        }
        if (filter.has("productId")) {
            searchFilter.and("product.id", filter.getString("productId"));
        }

        if (filter.has("fromDate") && filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("realizationDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("fromDate")) {
            searchFilter.and(String.format("realizationDate:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("fromDate"), DateTools.Resolution.DAY),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.DAY)));
        } else if (filter.has("toDate")) {
            Date toDate = filter.getDate("toDate");
            searchFilter.and(String.format("realizationDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }

        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Realization.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        if (filter.getSortColumn() != null)
            fullTextQuery.setSort(new Sort(new SortField(filter.getSortColumn(), SortField.Type.STRING, filter.getSortType())));
        else
            fullTextQuery.setSort(new Sort(new SortField("id", SortField.Type.STRING, true)));
        return new PageStream<_Realization>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }
}
