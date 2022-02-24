package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.product.ProductTypeRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ProductTypeDao;
import uz.uzcard.genesis.hibernate.entity._ProductType;
import uz.uzcard.genesis.service.ProductTypeService;

@Service
public class ProductTypeServiceImpl implements ProductTypeService {
    @Autowired
    private ProductTypeDao productTypeDao;

    @Override
    public PageStream<_ProductType> items(String name, Long id) {
        FilterParameters filter = new FilterParameters();
        if (name != null && name != "")
            filter.add("name", name);
        if (id != null)
            filter.add("id", "" + id);
        return productTypeDao.search(filter);
    }

    @Override
    public _ProductType save(ProductTypeRequest request) {
        _ProductType productType = productTypeDao.get(request.getId());
        if (productType == null) {
            productType = new _ProductType();
        }
        productType.setName(request.getName());
        productType.setParent(productTypeDao.get(request.getParentId()));
        return productTypeDao.save(productType);
    }

    @Override
    public void delete(Long id) {
        productTypeDao.delete(id);
    }

    @Override
    public PageStream<_ProductType> list(Long parentId, String name, Boolean isParent) {
        return productTypeDao.search(new FilterParameters() {{
            setSize(Integer.MAX_VALUE);
            setParentId(parentId);
            setName(name);
            addBoolean("isParent", isParent);
        }});
    }
}