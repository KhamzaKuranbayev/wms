/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import uz.uzcard.genesis.controller.order.OrderController;
import uz.uzcard.genesis.controller.order.OrderItemController;
import uz.uzcard.genesis.controller.order.OrderItemStatusController;
import uz.uzcard.genesis.controller.product.ProductController;
import uz.uzcard.genesis.dto.api.req.order.OrderItemAcceptRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemTenderRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderRequest;
import uz.uzcard.genesis.dto.api.req.setting.ItemCountRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.dao.ProductDao;
import uz.uzcard.genesis.hibernate.entity._Product;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static uz.uzcard.genesis.FileUtilsTest.getMultipartFile;

@Component
public class OrderTest {

    @Autowired
    protected ProductController productController;
    @Autowired
    private ProductTest productTest;
    @Autowired
    private UnitTypeTest unitTypeTest;
    @Autowired
    private OrderController orderController;
    @Autowired
    private OrderItemController orderItemController;
    @Autowired
    private OrderItemStatusController orderItemStatusController;
    @Autowired
    private ProductDao productDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long add(String product, String unitType, String fileName) throws IOException {
        Long unitTypeId = unitTypeTest.getByName(unitType);
        Long productId = getProductId(product, unitTypeId, fileName);

        CommonsMultipartFile multipartFile = getMultipartFile(fileName);

        SingleResponse<Map> response = orderController.add(new OrderRequest(null, productId, 10d, unitTypeId), Arrays.asList(multipartFile));
        assertTrue(response.isSuccess());
        return Long.parseLong("" + response.getData().get("id"));
    }

    private Long getProductId(String productName, Long unitTypeId, String fileName) {
        Long productId;
        try {
            productId = productTest.getProductId(productName);
        } catch (Throwable e) {
            productTest.add(productName, List.of(unitTypeId), fileName);
            productId = productTest.getProductId(productName);
        }
        return productId;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long addItem(Long orderId, String productName, String unitType, String fileName) throws IOException {
        Long unitTypeId = unitTypeTest.getByName(unitType);
        Long product_id = getProductId(productName, unitTypeId, fileName);

        CommonsMultipartFile multipartFile = getMultipartFile(fileName);
        _Product product = productDao.get(product_id);
        assertNotNull(product);
        assertFalse(product.getUnitTypes().isEmpty());

        SingleResponse<Map> response = orderItemController.addOrderItem(new OrderItemRequest() {{
            setOrder_id(orderId);
            setProduct_id(product_id);
            setUnitTypeId(product.getUnitTypes().get(0).getId());
            setCount((double) new Random().nextInt(20));
        }}, multipartFile);
        assertTrue(response.isSuccess());
        return Long.parseLong("" + response.getData().get("id"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendToOmtk(Long orderId) {
        SingleResponse response = orderController.sendToOMTK(orderId);
        assertTrue(response.isSuccess());
    }

    public void updateCount(Long orderItemId, Double count) {
        SingleResponse<Map> response = orderItemController.updateOrderItemCount(new ItemCountRequest(orderItemId, count));
        assertTrue(response.isSuccess());
        response = orderItemController.getItem(orderItemId);
        assertTrue(response.isSuccess());
        assertEquals(Double.parseDouble("" + response.getData().get("count")), count);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void accept(Long orderItemId) {
        SingleResponse response = orderItemStatusController.accept(new OrderItemAcceptRequest() {{
            setOrderItemId(orderItemId);
        }});
        assertTrue(response.isSuccess());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void specification(Long orderItemId) {
        SingleResponse response = orderItemStatusController.specification(new OrderItemTenderRequest() {{
            setOrderItemId(orderItemId);
        }});
        assertTrue(response.isSuccess());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tender(Long orderItemId) {
        SingleResponse response = orderItemStatusController.tender(new OrderItemTenderRequest() {{
            setOrderItemId(orderItemId);
        }});
        assertTrue(response.isSuccess());
    }
}*/
