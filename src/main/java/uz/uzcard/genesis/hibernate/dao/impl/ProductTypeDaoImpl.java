package uz.uzcard.genesis.hibernate.dao.impl;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ProductTypeDao;
import uz.uzcard.genesis.hibernate.entity._ProductType;
import uz.uzcard.genesis.hibernate.entity._State;

@Component(value = "productTypeDao")
public class ProductTypeDaoImpl extends DaoImpl<_ProductType> implements ProductTypeDao {
    public ProductTypeDaoImpl() {
        super(_ProductType.class);
    }

    @Override
    public PageStream<_ProductType> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("id"))
            searchFilter.and("id", filter.getString("id").trim());
        if (filter.has("name"))
            searchFilter.and("name", filter.getString("name").trim() + "*");
        if (filter.has("parentId"))
            searchFilter.and("parent.id", "" + filter.getParentId());
        if (filter.getBool("isParent"))
            searchFilter.and("parent:null");
        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _ProductType.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_ProductType>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }
}
