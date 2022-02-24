/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import uz.uzcard.genesis.controller.product.ProductController;
import uz.uzcard.genesis.dto.api.req.product.*;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.entity._Product;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static uz.uzcard.genesis.FileUtilsTest.getMultipartFile;

@Component
public class ProductTest {
    @Autowired
    private ProductController productController;

    public Long getProductId(String name) {
        ListResponse productList = productController.items(ProductFilterItemRequest.builder().name(name).limit(10).build());
        assertTrue(productList.getTotal() > 0);
        assertTrue(productList.getData().size() > 0);
        return Long.parseLong("" + ((Map) productList.getData().stream().findFirst().get()).get("id"));
    }

    //todo REQUIRES_NEW qo`yilishiga sabab: transaction commit bo`ladi va hibernate search reindex qiladi
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map add(String name, List<Long> unitTypes, String fileName) {

        CommonsMultipartFile multipartFile = null;
        try {
            multipartFile = getMultipartFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SingleResponse response = productController.save(ProductRequest.builder().name(name).unitTypeIds(unitTypes).build(), multipartFile);
        assertTrue(response.isSuccess());
        assertTrue(((Map<String, String>) response.getData()).containsKey("id"));
        return (Map) response.getData();
    }

    public Map getById(Long id) {
        SingleResponse response = productController.getById(id);
        assertNotNull(response.getData(), "Product Not found");
        return (Map) response.getData();
    }

    public Long check(String name) {
        ListResponse productList = productController.list(ProductFilterRequest.builder().name(name).limit(10).build());
        assertTrue(productList.getTotal() > 0);
        assertTrue(productList.getData().size() > 0);
        return Long.parseLong("" + ((Map) productList.getData().stream().findFirst().get()).get("id"));
    }

    public void delete(Long id) {
        SingleResponse response = productController.delete(id);
        assertTrue(response.isSuccess());
    }

    public void saveAttributes(Long productId, List<AttributeRequest> attributeRequests) {
        SingleResponse response = productController.saveAttributes(ProductAttributesRequest.builder().productId(productId).attributes(attributeRequests).build());
        assertNotNull(response.getData());
        assertNotNull(((Map<String, String>) response.getData()).containsKey("attributes"));
        assertTrue(response.isSuccess());
    }
}
*/
