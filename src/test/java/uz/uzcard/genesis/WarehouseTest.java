/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.controller.warehouse.WarehouseController;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.setting.WarehouseRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseFilterRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.dao.WarehouseDao;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Component
public class WarehouseTest {
    @Autowired
    private WarehouseController warehouseController;
    @Autowired
    private WarehouseDao warehouseDao;

    public void list() {
        ListResponse listResponse = warehouseController.items(null);
    }

    public SelectItem getFirst() {
        ListResponse response = warehouseController.items(null);
        if (response.getTotal() < 1)
            return null;
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());
        return (SelectItem) response.getData().stream().findAny().get();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SingleResponse add(WarehouseRequest request) {
        return warehouseController.save(request);
    }

    public void reindex(Long id) {
        warehouseDao.reindex(List.of(id));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void check(String name) {
        ListResponse response = warehouseController.list(WarehouseFilterRequest.builder().name(name).limit(10).build());
        assertTrue(response.isSuccess());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getData().isEmpty());
    }
}*/
