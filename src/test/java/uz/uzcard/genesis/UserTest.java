/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.controller.setting.UserController;
import uz.uzcard.genesis.dto.api.req.setting.ChangePasswordRequest;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.user.UserFilterRequest;
import uz.uzcard.genesis.dto.api.req.user.UserRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
public class UserTest {

    @Autowired
    private UserController userController;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map save(UserRequest request) {
        SingleResponse response = userController.save(request);
        assertTrue(response.isSuccess());
        assertTrue(((Map<String, String>) response.getData()).containsKey("id"));
        return (Map) response.getData();
    }

    @Transactional
    public List<Map<String, Object>> check(UserFilterRequest request) {
        ListResponse userList = userController.list(request);
        assertTrue(userList.getTotal() > 0);
        assertTrue(userList.getData().size() > 0);
        return (List<Map<String, Object>>) userList.getData();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean changePassword(ChangePasswordRequest request) {
        SingleResponse response = userController.changePassword(request);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long id) {
        SingleResponse response = userController.delete(DeleteRequest.builder().objectId(id).build());
        assertTrue(response.isSuccess());
        assertTrue((Boolean) response.getData());
    }
}
*/
