/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.controller.product.ProductTypeController;
import uz.uzcard.genesis.dto.api.req.product.ProductTypeRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.dao.ProductTypeDao;
import uz.uzcard.genesis.hibernate.entity._ProductType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

*/
/**
 * Created by norboboyev_h  on 14.12.2020  14:25
 *//*

@Component
public class ProductTypeTest {
    @Autowired
    private ProductTypeController productTypeController;
    @Autowired
    private ProductTypeDao productTypeDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long add(ProductTypeRequest request) {
        SingleResponse response = productTypeController.save(request);
        assertNotNull(response.getData());
        assertTrue(response.isSuccess());
        HashMap hashMap = (HashMap) response.getData();
        return Long.getLong(hashMap.get("id").toString());
    }

    @Transactional(readOnly = true)
    public List<Map<String, String>> list(Long parentId, String name) {
        ListResponse list = productTypeController.list(parentId, name);
        assertTrue(list.getTotal() > 0);
        assertTrue(list.getData().size() > 0);
        assertTrue(list.isSuccess());
        return (List<Map<String, String>>) list.getData();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long id) {
        SingleResponse response = productTypeController.delete(id);
        assertTrue(response.isSuccess());
//        assertTrue((Boolean) response.getData());
    }

    public void checkForDeleted(Long id) {
        _ProductType productType = productTypeDao.get(id);
        assertNull(productType);
    }

    public void check(String name) {
        ListResponse response = productTypeController.list(null, name);
        assertTrue(response.isSuccess());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getData().isEmpty());
    }

//    @Transactional(readOnly = true)
//    public Map getById(Long id) {
//        SingleResponse response = productTypeController.getById(id);
//        assertNotNull(response.getData(), "Product Group Not found");
//        return (Map) response.getData();
//    }
}
*/
