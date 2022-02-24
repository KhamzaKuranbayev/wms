/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.controller.product.UnitTypeController;
import uz.uzcard.genesis.dto.api.req.product.UnitTypeFilterRequest;
import uz.uzcard.genesis.dto.api.req.product.UnitTypeRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.entity._UnitType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Component
public class UnitTypeTest {

    @Autowired
    private UnitTypeController unitTypeController;

    public long getByName(String name) {
        ListResponse unitTypeList = unitTypeController.list(UnitTypeFilterRequest.builder().name(name).limit(10).build());
        assertTrue(unitTypeList.getTotal() > 0);
        assertTrue(unitTypeList.getData().size() > 0);
        return Long.parseLong("" + ((Map) unitTypeList.getData().stream().findFirst().get()).get("id"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map save(UnitTypeRequest request) {
        SingleResponse response = unitTypeController.save(request);
        assertNotNull(response.getData());
        assertTrue(((Map) response.getData()).containsKey("id"));
        assertTrue(response.isSuccess());
        return (Map) response.getData();
    }

    @Transactional(readOnly = true)
    public List<Map<String, String>> list(UnitTypeFilterRequest request) {
        ListResponse list = unitTypeController.list(request);
        assertTrue(list.getTotal() > 0);
        assertTrue(list.getData().size() > 0);
        assertTrue(list.isSuccess());
        return (List<Map<String, String>>) list.getData();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long id) {
        SingleResponse response = unitTypeController.delete(id);
        assertTrue(response.isSuccess());
    }
}
*/
