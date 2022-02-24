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
import uz.uzcard.genesis.hibernate.dao.ProductItemSummByDepartmentDao;
import uz.uzcard.genesis.hibernate.entity._ProductItemSummByDepartment;
import uz.uzcard.genesis.hibernate.entity._ProductType;
import uz.uzcard.genesis.hibernate.entity._State;

import java.util.Calendar;
import java.util.Date;

@Component(value = "productItemSummByDepartmentDao")
public class ProductItemSummByDepartmentDaoImpl extends DaoImpl<_ProductItemSummByDepartment> implements ProductItemSummByDepartmentDao {

    public ProductItemSummByDepartmentDaoImpl() {
        super(_ProductItemSummByDepartment.class);
    }

    @Override
    public PageStream<_ProductItemSummByDepartment> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("departmentId"))
            searchFilter.and("departmentId", filter.getString("departmentId").trim());

        if (filter.has("forDashboard")) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 00);
            cal.set(Calendar.MINUTE, 00);
            cal.set(Calendar.SECOND, 00);
            cal.set(Calendar.MILLISECOND, 00);
            Date fromDate = cal.getTime();

            cal.add(Calendar.MONTH, 1);
            Date toDate = cal.getTime();

            searchFilter.and(String.format("calculatedDate:[%s TO %s]",
                    DateTools.dateToString(fromDate, DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }
        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ProductItemSummByDepartment.class);
        fullTextQuery.setSort(new Sort(new SortField("calculatedDate", SortField.Type.STRING)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_ProductItemSummByDepartment>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }
}
