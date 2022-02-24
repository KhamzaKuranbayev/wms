package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.partition.PartitionCarriageAddressDto;
import uz.uzcard.genesis.dto.api.req.partition.PartitionFilterRequest;
import uz.uzcard.genesis.dto.api.req.partition.PartitionProductRequest;
import uz.uzcard.genesis.dto.api.req.partition.PartitionRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Partition;

import java.time.LocalDate;
import java.util.List;

public interface PartitionService {

    void reCalculate(_Partition partition);

    _Partition save(PartitionRequest request, Long lotId, boolean used);

    SingleResponse produce(PartitionProductRequest request);

    PageStream<_Partition> list(PartitionFilterRequest filter);

    void reCalculateProducts();

    _Partition get(Long contractItemId, Long warehouseId, LocalDate date);

    List<String> getExpiringPartitions(Long id,Long depId);

    PageStream<_Partition> dashboard(DashboardFilter filter);
}