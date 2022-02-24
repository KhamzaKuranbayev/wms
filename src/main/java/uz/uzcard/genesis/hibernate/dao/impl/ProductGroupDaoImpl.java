package uz.uzcard.genesis.hibernate.dao.impl;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ProductGroupDao;
import uz.uzcard.genesis.hibernate.entity._ProductGroup;
import uz.uzcard.genesis.hibernate.entity._State;

@Component(value = "productGroupDao")
public class ProductGroupDaoImpl extends DaoImpl<_ProductGroup> implements ProductGroupDao {
    public ProductGroupDaoImpl() {
        super(_ProductGroup.class);
    }

    @Override
    public PageStream<_ProductGroup> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("name"))
            searchFilter.and("name", filter.getString("name"));
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ProductGroup.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_ProductGroup>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }
}
