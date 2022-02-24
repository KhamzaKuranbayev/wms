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
//import uz.uzcard.genesis.hibernate.dao.AttributeDao;
//
//@Api(tags = "Migrations")
//@RestController
//@RequestMapping(value = "/api/attribute-fix")
//public class AttributeFixController {
//
//    @Autowired
//    private AttributeDao attributeDao;
//
//    @ApiOperation(value = "Get items")
//    @Transactional
//    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
//    public void itemLogics() {
//        attributeDao.list().forEach(attribute -> {
//            attribute.setItems(attribute.getItems2());
//            attributeDao.save(attribute);
//        });
//    }
//}
