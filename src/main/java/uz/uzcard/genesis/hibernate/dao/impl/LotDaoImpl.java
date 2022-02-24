package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ContractDao;
import uz.uzcard.genesis.hibernate.dao.ContractItemDao;
import uz.uzcard.genesis.hibernate.dao.LotDao;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.entity._Lot;
import uz.uzcard.genesis.hibernate.entity._State;

import java.util.List;

@Component
public class LotDaoImpl extends DaoImpl<_Lot> implements LotDao {
    @Autowired
    private ContractItemDao contractItemDao;
    @Autowired
    private ContractDao contractDao;

    public LotDaoImpl() {
        super(_Lot.class);
    }

    @Override
    public PageStream<_Lot> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("contractItemId")) {
            searchFilter.and("partition.contractItem.id", filter.getString("contractItemId"));
        }
        if (filter.has("lotName")) {
            searchFilter.and("name", filter.getString("lotName") + "*");
        }
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Lot.class);
        fullTextQuery.setSort(new Sort(new SortField("partition.date", SortField.Type.STRING),
                new SortField("auditInfo.creationDate", SortField.Type.STRING)));
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Lot>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _Lot save(_Lot entity) {
        super.save(entity);
        reCalculate(entity);
        return entity;
    }

    private void reCalculate(_Lot lot) {
        if (lot.getPartition() == null)
            return;
        _ContractItem contractItem = lot.getPartition().getContractItem();
        Double summa = (Double) findSingle("select sum(t.count) from _Lot t left join t.partition p left join p.contractItem ci " +
                        " where t.state != :deleted and p.state != :deleted and ci = :contractItem",
                preparing(new Entry("deleted", _State.DELETED), new Entry("contractItem", contractItem)));
        contractItem.setReceipt(summa == null ? 0 : summa);
        contractItemDao.save(contractItem);
        contractDao.reindex(List.of(contractItem.getParent().getId()));
    }
}