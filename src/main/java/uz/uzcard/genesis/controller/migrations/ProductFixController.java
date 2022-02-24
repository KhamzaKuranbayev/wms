//package uz.uzcard.genesis.controller.migrations;
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import uz.uzcard.genesis.hibernate.dao.ProductAttributeDao;
//import uz.uzcard.genesis.hibernate.dao.ProductDao;
//
//@Api(tags = "Migrations")
//@RestController
//@RequestMapping(value = "/api/product-fix")
//public class ProductFixController {
//
//    @Autowired
//    private ProductDao productDao;
//    @Autowired
//    private ProductAttributeDao productAttributeDao;
//
//    @ApiOperation(value = "Get items")
//    @Transactional
//    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
//    public void itemLogics() {
//        productDao.list().forEach(product -> {
//            product.setAttr(product.getAttr2());
//            productDao.save(product);
//        });
//        productAttributeDao.list().forEach(productAttribute -> {
//            productAttribute.setItems(productAttribute.getItems2());
//            productAttributeDao.save(productAttribute);
//        });
//    }
//}