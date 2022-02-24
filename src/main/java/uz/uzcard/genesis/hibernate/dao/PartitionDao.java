package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._Product;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public interface PartitionDao extends Dao<_Partition> {
    Double getRemainsByProduct(_Product product);

    _Partition get(Long contractItemId, Long warehouseId, LocalDate date);

    List<String> getExpiringPartitions(Long id, Long depId);

    List<String> getAllPartitions(Long depId);

    Stream<_Partition> findAll();

    Stream<_Partition> findAllByContractItem(_ContractItem contractItem);
}