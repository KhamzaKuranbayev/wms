package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.setting.SupplierFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.SupplierRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Supplier;

public interface SupplierService {
    PageStream<_Supplier> list(SupplierFilterRequest request);

    _Supplier save(SupplierRequest request);

    void delete(DeleteRequest request);
}
