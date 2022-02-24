package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ProduceHistoryDao;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._ProduceHistory;
import uz.uzcard.genesis.hibernate.entity._State;

/**
 * Created by norboboyev_h  on 25.12.2020  14:16
 */
@Component(value = "producingHistoryDao")
public class ProduceHistoryDaoImpl extends DaoImpl<_ProduceHistory> implements ProduceHistoryDao {
    public ProduceHistoryDaoImpl() {
        super(_ProduceHistory.class);
    }

    @Override
    public PageStream<_ProduceHistory> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("orderItemId")) {
            searchFilter.and("orderItem.id", filter.getString("orderItemId"));
        }

        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ProduceHistory.class);
        fullTextQuery.setSort(new Sort(new SortField("auditInfo.creationDate", SortField.Type.STRING, true)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        if ("remains".equals(filter.getSortColumn())) {
            fullTextQuery.setSort(new Sort(new SortField(filter.getSortColumn(), SortField.Type.DOUBLE, !filter.getSortType())));
        }
        fullTextQuery.setCacheable(true);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_ProduceHistory>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public double getCountByOrderItem(_OrderItem orderItem) {
        Double count = (Double) findSingle("select sum(t.count) from _ProduceHistory t where t.state <> :state and t.orderItem = :orderItem ",
                preparing(new Entry("orderItem", orderItem), new Entry("state", _State.DELETED)));
        return count == null ? 0 : count;
    }

    @Override
    public double getCountByOrderItemAndReqDone(_OrderItem orderItem) {
        Double count = (Double) findSingle("select sum(t.count) from _ProduceHistory t where t.state = :state and t.orderItem = :orderItem ",
                preparing(new Entry("orderItem", orderItem), new Entry("state", _State.REQUEST_DONE)));
        return count == null ? 0 : count;
    }

}
