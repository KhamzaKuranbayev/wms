package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.product.UnitTypeFilterRequest;
import uz.uzcard.genesis.dto.api.req.product.UnitTypeRequest;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.UnitTypeDao;
import uz.uzcard.genesis.hibernate.entity._UnitType;
import uz.uzcard.genesis.service.UnitTypeService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

@Service
public class UnitTypeServiceImpl implements UnitTypeService {

    @Autowired
    private UnitTypeDao unitTypeDao;

    @Override
    public _UnitType save(UnitTypeRequest request) {
        if (request.getNameEn() == null || request.getNameRu() == null || request.getNameUz() == null)
            throw new RpcException(GlobalizationExtentions.localication("NAME_REQUIRED"));
        _UnitType unitType;
        if (request.getId() == null) {
            unitType = new _UnitType();
        } else {
            unitType = unitTypeDao.get(request.getId());
            if (unitType == null)
                throw new RpcException(GlobalizationExtentions.localication("UNITTYPE_NOT_FOUND"));
        }
        unitType.setNameUz(request.getNameUz());
        unitType.setNameEn(request.getNameEn());
        unitType.setNameRu(request.getNameRu());
        unitType.setCountable(request.isCountable());
        return unitTypeDao.save(unitType);
    }

    @Override
    public PageStream<_UnitType> search(UnitTypeFilterRequest request) {
        return unitTypeDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            setName(request.getName());
        }}.add("name", request.getName()));
    }

    @Override
    public void delete(Long id) {
        if (id == null)
            throw new RpcException(GlobalizationExtentions.localication("ID_REQUIRED"));
        _UnitType unitType = unitTypeDao.getById(id);
        if (unitType == null)
            throw new RpcException(GlobalizationExtentions.localication("UNITTYPE_NOT_FOUND"));
        unitTypeDao.delete(id);
    }
}
