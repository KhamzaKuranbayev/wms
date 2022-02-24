/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.controller.setting.SupplierController;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.setting.SupplierFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.SupplierRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.dao.SupplierDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._Supplier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

*/
/**
 * Created by norboboyev_h  on 14.12.2020  14:25
 *//*

@Component
public class SupplierTest {

    @Autowired
    private SupplierController supplierController;

    @Autowired
    private SupplierDao supplierDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map save(SupplierRequest request) {
        SingleResponse response = supplierController.save(request);
        assertNotNull(response.getData());
        assertTrue(((Map) response.getData()).containsKey("id"));
        assertTrue(response.isSuccess());
        return (Map) response.getData();
    }

    @Transactional(readOnly = true)
    public List<Map<String, String>> list(SupplierFilterRequest filter) {
        ListResponse response = supplierController.list(filter);
        assertTrue(response.isSuccess());
        assertTrue(response.getTotal() > 0);
        assertTrue(response.getData().size() > 0);
        return (List<Map<String, String>>) response.getData();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long id) {
        SingleResponse response = supplierController.delete(DeleteRequest.builder().objectId(id).build());
        assertTrue(response.isSuccess());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long add(SupplierRequest request) {
        SingleResponse response = supplierController.save(request);
        assertNotNull(response.getData());
        assertTrue(response.isSuccess());
        Map supplier = (Map) response.getData();
        return Long.parseLong("" + supplier.get("id"));
    }

    public void check(SupplierFilterRequest filter) {
        ListResponse response = supplierController.list(filter);
        assertTrue(response.isSuccess());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getData().isEmpty());
    }

    public void checkForDeleted(Long id) {
        _Supplier supplier = supplierDao.get(id);
        assertEquals(supplier.getState(), _State.DELETED);
    }

    public Long getFirst() {
        ListResponse response = supplierController.list(SupplierFilterRequest.builder().limit(10).build());
        assertTrue(response.isSuccess());
        assertTrue(response.getTotal() > 0);
        assertTrue(response.getData().size() > 0);
        return Long.parseLong("" + ((Map) response.getData().stream().findFirst().get()).get("id"));
    }
}*/
