package uz.uzcard.genesis.controller.migrations;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzcard.genesis.service.OrderService;

@Api(tags = "Migrations")
@RestController
@RequestMapping(value = "/api/order-fix")
public class OrderFixController {
    @Autowired
    private OrderService orderService;

    @Transactional
    @GetMapping("/team-logics")
    public void orderTeam() {
        boolean empty = true;
        int index = 0, n = 100;
        do {
            empty = orderService.teamLogicsFix(index, n);
            index += n;
        } while (!empty);
    }
}