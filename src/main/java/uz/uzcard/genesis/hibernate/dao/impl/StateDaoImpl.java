package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.StateDao;
import uz.uzcard.genesis.hibernate.entity._State;

import java.util.stream.Stream;

@Component(value = "stateDao")
public class StateDaoImpl extends DaoImpl<_State> implements StateDao {
    public StateDaoImpl() {
        super(_State.class);
    }

    @Override
    public _State getByCode(String code) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("code", code);
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("getItems", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _State.class);
        fullTextQuery.setCacheable(true);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        return (_State) fullTextQuery.uniqueResult();
    }

    @Override
    public Stream<SelectItem> getItems(String entityName) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("(entityName:" + entityName.toLowerCase() + " OR -entityName:[* TO *])");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("getItems", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _State.class);
        fullTextQuery.setSort(new Sort(new SortField("entityName", SortField.Type.STRING)));
        fullTextQuery.setCacheable(true);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        return ((Stream<_State>) fullTextQuery.stream()).map(state -> new SelectItem(state.getId(), state.getName(), state.getCode()));
    }

    @Override
    public PageStream<_State> search(FilterParameters filter) {
        CustomSearchFilter searchFilter = new CustomSearchFilter(filter).invoke();
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.getQuery());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _State.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_State>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    private class CustomSearchFilter {
        private final FilterParameters filter;
        private String query;

        public CustomSearchFilter(FilterParameters filter) {
            this.filter = filter;
        }

        public CustomSearchFilter invoke() {
            SearchFilter searchFilter = new SearchFilter();
            if (filter.has("entityName")) {
                searchFilter.and("(entityName:" + filter.getString("entityName") + " OR entityName:null)");
            } else {
                searchFilter.and("entityName:null");
            }
            searchFilter.and("-state", _State.DELETED);
            query = searchFilter.toString();
            return this;
        }

        public String getQuery() {
            return query;
        }
    }
}