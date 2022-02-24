package uz.uzcard.genesis.hibernate.dao.impl;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.config.Constants;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.StillageDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._Stillage;

@Component(value = "stilalgeDao")
public class StillageDaoImpl extends DaoImpl<_Stillage> implements StillageDao {
    public StillageDaoImpl() {
        super(_Stillage.class);
    }

    @Override
    public PageStream<_Stillage> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("warehouseId"))
            searchFilter.and("warehouse.id", filter.getString("warehouseId"));
        if (filter.getId() != null)
            searchFilter.and("id", "" + filter.getId());
        if (filter.has("cellId"))
            searchFilter.and("cells.id", filter.getString("cellId"));
        if (filter.has("name"))
            searchFilter.and("name", filter.getString("name"));
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Stillage.class);
        fullTextQuery.initializeObjectsWith(
                ObjectLookupMethod.SECOND_LEVEL_CACHE,
                DatabaseRetrievalMethod.QUERY
        );
        fullTextQuery.setCacheable(true);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _Stillage getByCell(Long cellId) {
        return (_Stillage) findSingle("select t from _Stillage t left join t.cells c " +
                        " where t.state != :deleted and c.id = :cell",
                preparing(new Entry("deleted", _State.DELETED), new Entry("cell", cellId)), Constants.Cache.QUERY_STILLAGE);
    }
}