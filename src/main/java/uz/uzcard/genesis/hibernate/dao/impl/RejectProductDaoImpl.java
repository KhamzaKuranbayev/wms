package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.document.DateTools;
import org.hibernate.search.query.facet.Facet;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.RejectProductDao;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.entity._RejectProduct;
import uz.uzcard.genesis.hibernate.entity._State;

import java.util.List;
import java.util.stream.Stream;

@Component
public class RejectProductDaoImpl extends DaoImpl<_RejectProduct> implements RejectProductDao {
    public RejectProductDaoImpl() {
        super(_RejectProduct.class);
    }

    @Override
    public List<Facet> productRejectedList(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.getFromDate() != null && filter.getToDate() != null) {
            searchFilter.and(String.format("auditInfo.creationDate:[%s TO %s]",
                    DateTools.dateToString(filter.getFromDate(), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(filter.getToDate(), DateTools.Resolution.MILLISECOND)));
        } else if (filter.getFromDate() != null) {
            searchFilter.and(String.format("auditInfo.creationDate:[%s TO %s]",
                    DateTools.dateToString(filter.getFromDate(), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.MILLISECOND)));
        } else if (filter.getToDate() != null) {
            searchFilter.and(String.format("auditInfo.creationDate:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(filter.getToDate(), DateTools.Resolution.MILLISECOND)));
        }
        searchFilter.and("-state:DELETED");
        return getFacets(searchFilter.toString(), "supplier.nameFacet", 10000);
    }

    @Override
    public Stream<_RejectProduct> findByContractItem(_ContractItem contractItem) {
        return find("select t from _RejectProduct t join t.contractItem ci where t.state <> :deleted and ci = :contractItem ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("contractItem", contractItem)));
    }
}