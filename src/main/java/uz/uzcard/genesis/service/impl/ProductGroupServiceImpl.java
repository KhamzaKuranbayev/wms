package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.product.ProductGroupRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ProductGroupDao;
import uz.uzcard.genesis.hibernate.entity._ProductGroup;
import uz.uzcard.genesis.service.ProductGroupService;

@Service
public class ProductGroupServiceImpl implements ProductGroupService {
    @Autowired
    private ProductGroupDao productGroupDao;

    @Override
    public PageStream<_ProductGroup> items(String name, int page, int limit) {
        return productGroupDao.search(new FilterParameters() {{
            setStart(page * limit);
            setSize(limit);
        }}.add("name", name));
    }

    @Override
    public Long save(ProductGroupRequest request) {
        _ProductGroup productGroup = productGroupDao.get(request.getId());
        if (productGroup == null) {
            productGroup = new _ProductGroup();
        }
        productGroup.setName(request.getName());
        productGroupDao.save(productGroup);
        return productGroup.getId();
    }

    @Override
    public _ProductGroup getById(Long id) {
        return productGroupDao.get(id);
    }

    @Override
    public void delete(Long id) {
        productGroupDao.delete(id);
    }
}