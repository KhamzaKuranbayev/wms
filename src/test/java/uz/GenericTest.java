//package uz;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.RepeatedTest;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.transaction.annotation.Transactional;
//import uz.uzcard.genesis.Main;
//import uz.uzcard.genesis.TestControllerUtils;
//import uz.uzcard.genesis.hibernate.dao.UserDao;
//import uz.uzcard.genesis.hibernate.entity._User;
//
//import java.lang.reflect.Method;
//
//
//@Transactional
//@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class GenericTest {
//
//    @Autowired
//    private UserDao userDao;
//
//    @RepeatedTest(20)
//    @Test
//    @Transactional
//    public void addOrderItem() {
//        /*testController(ContractController.class);*/
//        /*testController(ContractItemController.class);*/
//        /*testController(CustomerController.class);*/
//        /*testController(LotController.class);*/
//        /*testController(OutGoingContractController.class);*/
//        /*testController(PartitionController.class);*/
//        /*testController(RealizationController.class);*/
//        /*testController(UserAgreementController.class);*/
//        /*testController(HistoryController.class);*/
//        /*testController(OrderController.class);*/
//        /*testController(OrderItemController.class);*/
////        testController(OrderItemPickUpTimeController.class);
////        testController(OrderItemStatusController.class);
////        testController(ProduceHistoryController.class);
////        testController(GivenProductsController.class);
////        testController(InventarizationController.class);
////        testController(InventarizationLogController.class);
////        testController(ProductController.class);
////        testController(ProductGroupController.class);
////        testController(ProductItemController.class);
////        testController(ProductTypeController.class);
////        testController(RentController.class);
////        testController(UnitTypeController.class);
////        testController(AttachmentController.class);
////        testController(AttributeController.class);
////        testController(BranchController.class);
////        testController(ColumnSettingsController.class);
////        testController(DepartmentController.class);
////        testController(ElectronDigitalSignatureController.class);
////        testController(JwtAuthenticationController.class);
////        testController(NotificationController.class);
////        testController(PackageTypeController.class);
////        testController(PermissionController.class);
////        testController(ReindexController.class);
////        testController(RoleController.class);
////        testController(StateController.class);
////        testController(SupplierController.class);
////        testController(UserController.class);
////        testController(CarriageController.class);
////        testController(StillageController.class);
////        testController(WarehouseConroller.class);
//    }
//
//    private void testController(Class clazz) {
//        for (Method methodName : clazz.getDeclaredMethods()) {
//            TestControllerUtils.change(clazz, methodName.getName());
//        }
//    }
//
//    @BeforeEach
//    private void login() {
//        _User user = userDao.getByUseName("admin");
//        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, "123", user.getAuthorities()));
//    }
//}
