package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.report.ReportFilterRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ReportService {
    void getAllProducts(ReportFilterRequest filterRequest, HttpServletRequest request, HttpServletResponse response);

    void getAllProductByType(ReportFilterRequest filter, HttpServletRequest request, HttpServletResponse response);
}
