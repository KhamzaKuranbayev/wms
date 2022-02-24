package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.OrderItemPickUpTimeDao;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.Date;

/**
 * Madaminov Javohir {02.12.2020}.
 */
@Component(value = "orderItemPickUpTimeDao")
public class OrderItemPickUpTimeDaoImpl extends DaoImpl<_OrderItemPickUpTime> implements OrderItemPickUpTimeDao {
    public OrderItemPickUpTimeDaoImpl() {
        super(_OrderItemPickUpTime.class);
    }

    @Override
    public PageStream<_OrderItemPickUpTime> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("orderItemId")) {
            searchFilter.and("orderItem.id", filter.getString("orderItemId"));
        }

        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");

        searchFilter.and("-state", _State.DELETED);

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _OrderItemPickUpTime.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());

        return new PageStream<_OrderItemPickUpTime>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _OrderItemPickUpTime getById(Long id) {
        return (_OrderItemPickUpTime) findSingle("select t from _OrderItemPickUpTime t where t.state <> :deleted and t.id = :id"
                , preparing(new Entry("deleted", _State.DELETED), new Entry("id", id)));
    }
}
