/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.controller.setting.AttributeController;
import uz.uzcard.genesis.dto.api.req.product.UnitTypeRequest;
import uz.uzcard.genesis.dto.api.req.setting.AttributeFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.AttributeRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.entity._Attribute;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Component
public class AttributeTest {

    @Autowired
    private AttributeController attributeController;

    @Transactional(readOnly = true)
    public List<Map<String, String>> check(AttributeFilterRequest request) {
        ListResponse list = attributeController.list(request);
        assertTrue(list.getTotal() > 0);
        assertTrue(list.getData().size() > 0);
        assertTrue(list.isSuccess());
        return (List<Map<String, String>>) list.getData();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public _Attribute save(AttributeRequest request) {
        SingleResponse response = attributeController.save(request);
        assertNotNull(response.getData());
        assertNotNull(((_Attribute) response.getData()).getId());
        assertTrue(response.isSuccess());
        return (_Attribute) response.getData();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long id) {
        SingleResponse response = attributeController.delete(id);
        assertTrue(response.isSuccess());
    }
}
*/
