package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.setting.AttributeFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.AttributeRequest;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.AttributeDao;
import uz.uzcard.genesis.hibernate.entity._Attribute;
import uz.uzcard.genesis.service.AttributeService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

@Service
public class AttributeServiceImpl implements AttributeService {
    @Autowired
    private AttributeDao attributeDao;

    @Override
    public PageStream<_Attribute> list(AttributeFilterRequest request) {
        return attributeDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            setName(request.getName());
            setSortColumn(request.getSortBy());
            setSortType(request.getSortDirection());
        }});
    }

    @Override
    public _Attribute save(AttributeRequest request) {
        _Attribute attribute = attributeDao.get(request.getId());
        if (attribute == null) {
            attribute = new _Attribute();
            if (attributeDao.checkByName(request.getName()))
                throw new ValidatorException(GlobalizationExtentions.localication("NAME_ALREADY_EXISTS"));
        } else {
            if (attributeDao.checkByNameAndOwn(attribute.getId(), request.getName()))
                throw new ValidatorException(GlobalizationExtentions.localication("NAME_ALREADY_EXISTS"));
        }
        attribute.setName(request.getName());
        attribute.setItems(request.getItems());
        attributeDao.save(attribute);
        return attribute;
    }

    @Override
    public void delete(Long id) {
        if (id == null)
            throw new RpcException(GlobalizationExtentions.localication("ID_REQUIRED"));
        _Attribute attribute = attributeDao.getById(id);
        if (attribute == null)
            throw new RpcException(GlobalizationExtentions.localication("ATTRIBUTE_NOT_FOUND"));
        attributeDao.delete(id);
    }
}
