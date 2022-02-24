package uz.uzcard.genesis.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;
import uz.uzcard.genesis.dto.report.ReportByTypeDto;
import uz.uzcard.genesis.dto.report.ReportDto;
import uz.uzcard.genesis.dto.report.ReportFilterRequest;
import uz.uzcard.genesis.hibernate.dao.DepartmentDao;
import uz.uzcard.genesis.service.ReportService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private VelocityEngine velocityEngine;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void getAllProducts(ReportFilterRequest filterRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> filter = filter(filterRequest);

        String data = departmentDao.call(filter, "getreport", Types.VARCHAR);

        List<ReportDto> reportDtos = new ArrayList<>();
        if (data != null) {
            reportDtos = fromStringToNodes(data, new TypeReference<List<ReportDto>>() {
            });
        }

        Double newCount = new Double(0);
        Double producedCount = new Double(0);
        Double lastCount = new Double(0);
        Double total = new Double(0);
        if (reportDtos != null) {
            for (ReportDto reportDto : reportDtos) {
                if (reportDto.getNewProductCount() != null)
                    newCount += reportDto.getNewProductCount();
                if (reportDto.getProducedProductCount() != null)
                    producedCount += reportDto.getProducedProductCount();
                if (reportDto.getLastProductCount() != null)
                    lastCount += reportDto.getLastProductCount();
                if (reportDto.getTotal() != null)
                    total += reportDto.getTotal();
            }
        }

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("data", reportDtos);
        mapData.put("from_to_date", reportDtos);
        mapData.put("newTotalCount", newCount);
        mapData.put("lastTotalCount", lastCount);
        mapData.put("producedTotalCount", producedCount);
        mapData.put("totalCount", total);

        mapData.put("date", getDateFormat(filterRequest.getFromDate(), filterRequest.getToDate(), " оралиғидаги  Материаллар ҳисоботи"));

        String stringHtml = generateClassFile("report/full_report.vm", mapData);
        String filePath = getUniqueFilePath("Full Report ");

        response.setContentType("application/ms-excel");
        response.addHeader("Content-Disposition", String.format("attachment; filename=%s.xls", filePath));
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(stringHtml.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @SneakyThrows
    @Override
    public void getAllProductByType(ReportFilterRequest filterRequest, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> filter = filter(filterRequest);

        String data = departmentDao.call(filter, "getreportByType", Types.VARCHAR);

        List<ReportByTypeDto> byTypeDtos = fromStringToNodes(data, new TypeReference<List<ReportByTypeDto>>() {
        });

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("data", byTypeDtos);

        String stringHtml = generateClassFile("report/report_by_type.vm", mapData);
        String filePath = getUniqueFilePath("Full Report by product type");

        response.setContentType("application/ms-excel");
        response.addHeader("Content-Disposition", String.format("attachment; filename=%s.xlsx", filePath));
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(stringHtml.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    private <T> List<T> fromStringToNodes(String data, TypeReference typeReference) {
        try {
            return (List<T>) objectMapper.readValue(data, typeReference);
        } catch (Exception e) {

            throw new RuntimeException(String.format("could not parse string data %s", data));
        }
    }

    private Map<String, String> filter(ReportFilterRequest filterRequest) {
        Map<String, String> filter = new HashMap<>();
        if (filterRequest.getFromDate() != null)
            filter.put("fromDate", "" + filterRequest.getFromDate());
        if (filterRequest.getToDate() != null)
            filter.put("toDate", "" + filterRequest.getToDate());
        if (filterRequest.getProductId() != null)
            filter.put("productId", "" + filterRequest.getProductId());
        return filter;
    }

    private String generateClassFile(String template, Map<String, Object> map) {
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template, "UTF-8", map);
    }

    private String getUniqueFilePath(String filename) throws UnsupportedEncodingException {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hh = now.getHour();
        int mm = now.getMinute();
        return URLEncoder.encode(filename + year + "_" + month + "_" + day + "_" + hh + "_" + mm, "UTF-8");
    }

    // 2020 йил 15-январ 2021 йил 15-феврал оралиғидаги  Материаллар ҳисоботи
    private String getDateFormat(Date fromDate, Date toDate, String text) {
        String fromName = GlobalizationExtentions.getMonth(fromDate.getMonth());
        String toName = GlobalizationExtentions.getMonth(toDate.getMonth());
        return new String((fromDate.getYear() + 1900) + " йил " + fromDate.getDate() + "-" + fromName
                + " " + (toDate.getYear() + 1900) + " йил " + toDate.getDate() + "-" + toName + text);
    }
}
