/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.controller.setting.DepartmentController;
import uz.uzcard.genesis.dto.api.req.setting.DepartmentFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.DepartmentRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseFilterRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
public class DepartmentTest {
    @Autowired
    private DepartmentController departmentController;

    public void list() {
        departmentController.list(DepartmentFilterRequest.builder().limit(10).build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(DepartmentRequest request) {
        SingleResponse response = departmentController.save(request);
        assertTrue(response.isSuccess());
    }

    public void check(String name) {
        ListResponse response = departmentController.list(DepartmentFilterRequest.builder().name(name).limit(10).build());
        assertTrue(response.isSuccess());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getData().isEmpty());
    }
}*/
