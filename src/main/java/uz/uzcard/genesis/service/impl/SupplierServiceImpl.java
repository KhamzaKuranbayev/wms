package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.setting.SupplierFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.SupplierRequest;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.SupplierDao;
import uz.uzcard.genesis.hibernate.entity._Supplier;
import uz.uzcard.genesis.service.SupplierService;

@Service
public class SupplierServiceImpl implements SupplierService {
    @Autowired
    private SupplierDao supplierDao;

    @Override
    public PageStream<_Supplier> list(SupplierFilterRequest request) {
        return supplierDao.search(new FilterParameters() {{
            add("name", request.getName());
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
        }}.add("name", request.getName()));
    }

    @Override
    public _Supplier save(SupplierRequest request) {
        _Supplier supplier = supplierDao.get(request.getId());
        if (supplier == null) {
            supplier = new _Supplier();
        }
        supplier.setName(request.getName());
        return supplierDao.save(supplier);
    }

    @Override
    public void delete(DeleteRequest request) {
        if (request == null)
            throw new ValidatorException("Поставщик топилмади");
        _Supplier supplier = supplierDao.get(request.getObjectId());
        if (supplier == null)
            throw new ValidatorException("Поставщик топилмади");
        supplierDao.delete(supplier);
    }
}
