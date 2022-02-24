package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Contract;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.entity._Product;
import uz.uzcard.genesis.hibernate.entity._UnitType;

import java.util.stream.Stream;

public interface ContractItemDao extends Dao<_ContractItem> {
    int getAcceptCount(_Contract contract);

    int getMaxNumb(_Contract contract);

    Stream<_ContractItem> getByContract(_Contract contract);

    Stream<_ContractItem> findByParent(_Contract contract);

    int getTotalCount(_Contract contract);

    _ContractItem getByContractAndProduct(_Contract contract, _Product product, _UnitType unitType);

    Stream<_ContractItem> findAllByParent(_Contract contract);
}