package uz.uzcard.genesis.hibernate.dao.impl;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.SupplierDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._Supplier;

@Component(value = "supplierDao")
public class SupplierDaoImpl extends DaoImpl<_Supplier> implements SupplierDao {
    public SupplierDaoImpl() {
        super(_Supplier.class);
    }

    @Override
    public PageStream<_Supplier> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("name")) {
            searchFilter.and("name", filter.getString("name").toLowerCase().trim() + "*");
        }
        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Supplier.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Supplier>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }
}