package uz.uzcard.genesis.hibernate.dao;

import org.hibernate.search.query.facet.Facet;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.entity._RejectProduct;

import java.util.List;
import java.util.stream.Stream;

public interface RejectProductDao extends Dao<_RejectProduct> {
    List<Facet> productRejectedList(FilterParameters filter);

    Stream<_RejectProduct> findByContractItem(_ContractItem contractItem);
}