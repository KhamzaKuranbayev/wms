//package uz;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Order;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.web.server.LocalServerPort;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import uz.uzcard.genesis.*;
//import uz.uzcard.genesis.controller.setting.ReindexController;
//import uz.uzcard.genesis.dto.SelectItem;
//import uz.uzcard.genesis.dto.api.req.product.AttributeRequest;
//import uz.uzcard.genesis.dto.api.req.product.*;
//import uz.uzcard.genesis.dto.api.req.setting.*;
//import uz.uzcard.genesis.dto.api.resp.SingleResponse;
//import uz.uzcard.genesis.exception.ValidatorException;
//import uz.uzcard.genesis.hibernate.dao.UserDao;
//import uz.uzcard.genesis.hibernate.entity._Attribute;
//import uz.uzcard.genesis.hibernate.entity._User;
//import uz.uzcard.genesis.hibernate.enums.OrderClassification;
//import uz.uzcard.genesis.uitls.ServerUtils;
//
//import java.util.*;
//
//@Transactional
//@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class JUnitTest {
//    private static final String FILE_NAME = "D:\\test.png";
//    @LocalServerPort
//    int randomServerPort;
//
//    @Autowired
//    private UserDao userDao;
//    @Autowired
//    private ReindexController reindexController;
//    @Autowired
//    private ProductTest productTest;
//    @Autowired
//    private UnitTypeTest unitTypeTest;
//    @Autowired
//    private OrderTest orderTest;
//    @Autowired
//    private WarehouseTest warehouseTest;
//    @Autowired
//    private DepartmentTest departmentTest;
//    @Autowired
//    private AttributeTest attributeTest;
//    @Autowired
//    private UserTest userTest;
//    @Autowired
//    private ProductGroupTest productGroupTest;
//    @Autowired
//    private SupplierTest supplierTest;
//    @Autowired
//    private ProductTypeTest productTypeTest;
//    @Autowired
//    private ContractTest contractTest;
//
//    @Test
//    @Order(value = 999)
//    public void reindex() throws Exception {
////        reindexController.order(true);
////        reindexController.unitType(true);
////        reindexController.product(true);
////        reindexController.unitType(true);
////        reindexController.warehouse(true);
////        reindexController.warehouse(true);
//        reindexController.supplier(true);
//        reindexController.order(true);
//    }
//
//    @Test
//    public void department() {
//        String departmentName = ServerUtils.generateUniqueCode();
//        SelectItem warehouse = warehouseTest.getFirst();
//        departmentTest.add(DepartmentRequest.builder().name(departmentName).warehouseId(warehouse.getId()).depType(OrderClassification.DEPARTMENT).build());
//        departmentTest.check(departmentName);
//    }
//
//    @Test
//    public void productGroup() {
//        String name = ServerUtils.generateUniqueCode();
//        Long id = productGroupTest.add(ProductGroupRequest.builder().name(name).build());
//        productGroupTest.check(name);
//        productGroupTest.delete(id);
//        productGroupTest.checkForDeleted(id);
//    }
//
//    @Test
//    public void supplier() {
//        String name = ServerUtils.generateUniqueCode();
//        Long id = supplierTest.add(SupplierRequest.builder().name(name).build());
//        supplierTest.check(SupplierFilterRequest.builder().name(name).limit(10).build());
//        supplierTest.delete(id);
//        userDao.getSession().flush();
//        userDao.getSession().clear();
//        supplierTest.checkForDeleted(id);
//    }
//
//    @Test
//    public void productType() {
//        String name = ServerUtils.generateUniqueCode();
//        Long id = productTypeTest.add(ProductTypeRequest.builder().name(name).build());
//        productTypeTest.check(name);
//        productTypeTest.delete(id);
//        productTypeTest.checkForDeleted(id);
//    }
//
//    @Test
//    public void warehouse() {
//        String warehouseName = ServerUtils.generateUniqueCode();
//        SingleResponse warehouse = warehouseTest.add(WarehouseRequest.builder().name(warehouseName).build());
////        if (warehouse.isSuccess()) {
////            Long id = Long.parseLong("" + ((Map) warehouse.getData()).get("id"));
////            warehouseTest.reindex(id);
////        }
//        warehouseTest.check(warehouseName);
//    }
//
//    @Test
//    public void order() throws Exception {
//        Long orderId = orderTest.add("test", "kg", "D:\\test.png");
//        Long orderItemId = orderTest.addItem(orderId, "test2", "Kg", "D:\\test.png");
//        orderTest.updateCount(orderItemId, 23d);
//        orderTest.sendToOmtk(orderId);
//        try {
//            orderTest.accept(orderItemId);
//            throw new Exception("xatolik");
//        } catch (ValidatorException ignored) {
//        }
//        orderTest.specification(orderItemId);
//        orderTest.tender(orderItemId);
//        contractTest.add(orderItemId, FILE_NAME);
//    }
//
//    @Test
//    public void attribute() {
//        reindexController.attribute(true);
//        // create items
//        List<String> items = new ArrayList<>() {{
//            add("Red");
//            add("Black");
//            add("Blue");
//        }};
//        String name = ServerUtils.generateUniqueCode();
//        // save
//        _Attribute attribute = attributeTest.save(uz.uzcard.genesis.dto.api.req.setting.AttributeRequest.builder().name(name).items(items).build());
//        // list
//        List<Map<String, String>> attributes = attributeTest.check(AttributeFilterRequest.builder().limit(10).build());
//        Long id = attribute.getId();
//        // update
//        attributeTest.save(uz.uzcard.genesis.dto.api.req.setting.AttributeRequest.builder().id(id).name("Color1").items(items).build());
//        // delete
//        attributeTest.delete(id);
//        System.out.println("Success Attribute");
//    }
//
//    @Test
//    public void unitType() {
//        // save
//        Map<String, String> save = unitTypeTest.save(UnitTypeRequest.builder().nameUz("Test").nameEn("Test").nameRu("Test").build());
//        // list
//        List<Map<String, String>> unitTypes = unitTypeTest.list(UnitTypeFilterRequest.builder().name("Test").limit(10).build());
//        // update
//        Map<String, String> update = unitTypeTest.save(UnitTypeRequest.builder().id(Long.valueOf(save.get("id"))).nameUz("Test2").nameEn("Test2").nameRu("Test2").build());
//        unitTypeTest.delete(Long.valueOf(update.get("id")));
//        System.out.println("Success Unit Type");
//    }
//
//    @Test
//    public void product() {
//        List<String> items = new ArrayList<>() {{
//            add("Red");
//            add("Black");
//            add("Blue");
//        }};
//        String name = "" + new Double(new Random().nextDouble() * 1000000).longValue();
//
//        // save attribute
//        _Attribute attribute = attributeTest.save(uz.uzcard.genesis.dto.api.req.setting.AttributeRequest.builder().name(name).items(items).build());
//        // save unit type
//        Map<String, String> unittype = unitTypeTest.save(UnitTypeRequest.builder().nameUz("m3").nameEn("m3").nameRu("m3").build());
//
//        // save product
//        Map<String, String> product = productTest.add("Test with unittype", Arrays.asList(Long.valueOf(unittype.get("id"))), new String());
//
//        // get product by id
//        Map<String, String> productGetById = productTest.getById(Long.valueOf(product.get("id")));
//
//        // get list
//        Long productIdByName = productTest.check("Test with unittype");
//
//        // add attribute to product
//        productTest.saveAttributes(productIdByName, Arrays.asList(AttributeRequest.builder().id(attribute.getId()).items(attribute.getItems()).build()));
//
//        productTest.delete(productIdByName);
//
//        System.out.println("Success product");
//    }
//
//    @Test
//    public void user() {
//        // add
//        String userName = ServerUtils.generateUniqueCode();
//        Map<String, Object> user = userTest.save(UserRequest.builder().userName(userName).password("test")
//                .firstName("Test").lastName("Test").middleName("Test")
//                .phone(ServerUtils.generateUniqueCode()).email(ServerUtils.generateUniqueCode() + "testtest@gmail.com")
//                .build());
//
//        // list
//        List<Map<String, Object>> users = userTest.check(UserFilterRequest.builder().limit(10).build());
//
//        // change password
//        userTest.changePassword(ChangePasswordRequest.builder().username(userName)
//                .oldPassword("test").newPassword("test1").build()
//        );
//
//        // delete
//        userTest.delete(Long.valueOf(Long.valueOf(user.get("id").toString())));
//        System.out.println("Success User");
//    }
//
//    @BeforeEach
//    private void login() {
//        _User user = userDao.getByUseName("admin");
//        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, "123", user.getAuthorities()));
//    }
//}