/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.controller.product.ProductGroupController;
import uz.uzcard.genesis.dto.api.req.product.ProductGroupRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

*/
/**
 * Created by norboboyev_h  on 14.12.2020  14:27
 *//*

@Component
public class ProductGroupTest {

    @Autowired
    private ProductGroupController productGroupController;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long add(ProductGroupRequest request) {
        SingleResponse response = productGroupController.save(request);
        assertNotNull(response.getData());
        assertTrue(response.isSuccess());
        return (Long) response.getData();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long id) {
        SingleResponse response = productGroupController.delete(id);
        assertTrue(response.isSuccess());
//        assertTrue((Boolean) response.getData());
    }

    @Transactional(readOnly = true)
    public Map getById(Long id) {
        SingleResponse response = productGroupController.getById(id);
        assertNotNull(response.getData(), "Product Group Not found");
        return (Map) response.getData();
    }

    public void check(String name) {
        ListResponse response = productGroupController.items(name, 0, 10);
        assertTrue(response.isSuccess());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getData().isEmpty());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkForDeleted(Long id) {
        SingleResponse response = productGroupController.getById(id);
        assertTrue(response.isSuccess());
//        assertNull(response.getData());
    }
}
*/
