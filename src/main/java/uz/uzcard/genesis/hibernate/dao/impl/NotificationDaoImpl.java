package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.hibernate.search.query.facet.Facet;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.NotificationDao;
import uz.uzcard.genesis.hibernate.entity._Notification;
import uz.uzcard.genesis.hibernate.entity._State;

import java.util.List;

/**
 * Created by norboboyev_h  on 24.12.2020  11:59
 */
@Component(value = "notificationDao")
public class NotificationDaoImpl extends DaoImpl<_Notification> implements NotificationDao {
    public NotificationDaoImpl() {
        super(_Notification.class);
    }

    @Override
    public PageStream<_Notification> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("sentTo"))
            searchFilter.and("sentTo.id", filter.getString("sentTo"));
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Notification.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setSort(new Sort(new SortField("auditInfo.creationDate", SortField.Type.STRING, true)));
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Notification>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public int getUnreadMessageCount() {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("sentTo.id", "" + getUser().getId());
        searchFilter.and("read:false");
        List<Facet> facet = getFacets(searchFilter.toString(), "sentTo.id2", 10);
        if (facet.isEmpty())
            return 0;
        return facet.get(0).getCount();
    }
}