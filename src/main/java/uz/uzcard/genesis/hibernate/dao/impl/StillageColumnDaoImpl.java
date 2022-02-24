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
import uz.uzcard.genesis.hibernate.dao.StillageColumnDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._Stillage;
import uz.uzcard.genesis.hibernate.entity._StillageColumn;

import java.util.stream.Stream;

@Component(value = "stillageColumnDao")
public class StillageColumnDaoImpl extends DaoImpl<_StillageColumn> implements StillageColumnDao {
    public StillageColumnDaoImpl() {
        super(_StillageColumn.class);
    }

    @Override
    public PageStream<_StillageColumn> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("stillageId"))
            searchFilter.and("stillage.id", filter.getString("stillageId"));
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");

        searchFilter.and("-state", _State.DELETED);

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _StillageColumn.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_StillageColumn>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _StillageColumn getByCode(_Stillage stillage, String code) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("stillage.id", "" + stillage.getId());
        searchFilter.and("code", code);
        searchFilter.and("-state", _State.DELETED);

        org.apache.lucene.search.Query luceneQuery = queryParser("getByCode", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _StillageColumn.class);
        fullTextQuery.initializeObjectsWith(
                ObjectLookupMethod.SECOND_LEVEL_CACHE,
                DatabaseRetrievalMethod.QUERY
        );
        fullTextQuery.setCacheable(true);
        return (_StillageColumn) fullTextQuery.setMaxResults(1).uniqueResult();
    }

    @Override
    public Stream<_StillageColumn> findByStillage(_Stillage stillage) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("stillage.id", "" + stillage.getId());
        searchFilter.and("-state", _State.DELETED);

        org.apache.lucene.search.Query luceneQuery = queryParser("getByCode", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _StillageColumn.class);
        fullTextQuery.setSort(new Sort(new SortField("sortOrder", SortField.Type.INT)));
        fullTextQuery.initializeObjectsWith(
                ObjectLookupMethod.SECOND_LEVEL_CACHE,
                DatabaseRetrievalMethod.QUERY
        );
        fullTextQuery.setCacheable(true);
        return fullTextQuery.stream();
    }

    @Override
    public int getMaxOrderByStillage(_Stillage stillage) {
        if (stillage == null)
            return 0;
        if (stillage.isNew())
            return stillage.getColumns().size();
        Integer numb = (Integer) findSingle("select max(t.sortOrder) from _StillageColumn t where t.stillage = :stillage and t.state <> :deleted",
                preparing(new Entry("stillage", stillage), new Entry("deleted", _State.DELETED)));
        if (numb == null) return 0;
        return numb.intValue();
    }

}