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
import uz.uzcard.genesis.hibernate.dao.RoleDao;
import uz.uzcard.genesis.hibernate.entity._Role;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._UnitType;

import java.util.Set;
import java.util.stream.Stream;

@Component(value = "roleDao")
public class RoleDaoImpl extends DaoImpl<_Role> implements RoleDao {
    public RoleDaoImpl() {
        super(_Role.class);
    }

    @Override
    public PageStream<_Role> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("code")) {
            searchFilter.and("code", filter.getString("code").toLowerCase().trim() + "*");
        }
        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Role.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setSort(new Sort(new SortField("id", SortField.Type.STRING, true)));
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Role>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _Role getByCode(String code) {
        return (_Role) findSingle("select t from _Role t where t.code = :code and t.state <> :deleted",
                preparing(new Entry("code", code), new Entry("deleted", _State.DELETED)));
    }

    @Override
    public Stream<_Role> findByIds(Set<Long> ids) {
        return find("select t from _Role t where t.id in (:ids) and t.state <> 'DELETED' ",
                preparing(new Entry("ids", ids)));
    }
}