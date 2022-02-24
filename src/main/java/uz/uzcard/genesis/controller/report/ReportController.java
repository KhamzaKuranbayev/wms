package uz.uzcard.genesis.controller.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uz.uzcard.genesis.dto.report.ReportFilterRequest;
import uz.uzcard.genesis.service.ReportService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/all-products", method = RequestMethod.GET)
    public void getAllProducts(ReportFilterRequest filter, HttpServletRequest request, HttpServletResponse response) {
        reportService.getAllProducts(filter, request, response);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/product-type", method = RequestMethod.GET)
    public void getAllProductByType(ReportFilterRequest filter, HttpServletRequest request, HttpServletResponse response) {
        reportService.getAllProductByType(filter, request, response);
    }
}
