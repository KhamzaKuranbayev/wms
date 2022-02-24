package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.partition.PartitionCarriageAddressDto;
import uz.uzcard.genesis.dto.api.req.warehouse.*;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Carriage;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._StillageColumn;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by norboboyev_h  on 19.08.2020  17:49
 */

public interface CarriageService {

    PageStream<_Carriage> list(CarriageFilterRequest request);

    _Carriage getSingle(@NotNull Long id);

    _Carriage create(_StillageColumn stillageColumn, CarriageRequest request);

    void put(PutToCarriageRequest request);

    Boolean full(CarriageIsFullRequest request);

    void checkToHasProduct(List<_Carriage> carriages);

    PartitionCarriageAddressDto getAddress(_Carriage carriage);

    PageStream<_Carriage> getForMarkingAsFullOrNot(List<Long> ids);

    void delete(Long id);

    List<PartitionCarriageAddressDto> getAddresses(List<Long> carriages_id);

    Stream<_Carriage> findByIds(List<Long> carriagesId);

    LinkedHashSet<Long> searchByProduct(WarehouseFilterRequest request);

    boolean setupSize(CarriageSizeRequest request);
}
