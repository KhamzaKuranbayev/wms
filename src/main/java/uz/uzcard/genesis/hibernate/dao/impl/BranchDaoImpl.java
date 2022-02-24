package uz.uzcard.genesis.hibernate.dao.impl;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.BranchDao;
import uz.uzcard.genesis.hibernate.entity._Branch;

import java.util.stream.Stream;

@Component
public class BranchDaoImpl extends DaoImpl<_Branch> implements BranchDao {
    public BranchDaoImpl() {
        super(_Branch.class);
    }

    @Override
    public Stream<_Branch> findParents() {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("parent:null");

        org.apache.lucene.search.Query luceneQuery = queryParser("parents", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Branch.class);
        fullTextQuery.initializeObjectsWith(
                ObjectLookupMethod.SECOND_LEVEL_CACHE,
                DatabaseRetrievalMethod.QUERY
        );
        fullTextQuery.setCacheable(true);
        return fullTextQuery.stream();
    }

    @Override
    public _Branch getByMfo(String mfo) {
        if (mfo == null) return null;

        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("mfo", mfo);

        org.apache.lucene.search.Query luceneQuery = queryParser("getByMfo", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Branch.class);
        fullTextQuery.initializeObjectsWith(
                ObjectLookupMethod.SECOND_LEVEL_CACHE,
                DatabaseRetrievalMethod.QUERY
        );
        fullTextQuery.setCacheable(true);
        return (_Branch) fullTextQuery.uniqueResult();
    }

    @Override
    public Stream<_Branch> findChild(String mfo) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("parent.mfo", mfo);

        org.apache.lucene.search.Query luceneQuery = queryParser("parents", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Branch.class);
        fullTextQuery.initializeObjectsWith(
                ObjectLookupMethod.SECOND_LEVEL_CACHE,
                DatabaseRetrievalMethod.QUERY
        );
        fullTextQuery.setCacheable(true);
        return fullTextQuery.stream();
    }
}
