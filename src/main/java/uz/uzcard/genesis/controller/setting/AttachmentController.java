package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.api.resp.PdfImageResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.dto.file.AttachmentDto;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.entity._Attachment;
import uz.uzcard.genesis.service.AttachmentService;
import uz.uzcard.genesis.service.PdfService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "Attachment")
@Controller
@RequestMapping(value = "/api/attachment")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private PdfService pdfService;

    @ResponseBody
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/base64/{name}", produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
    public SingleResponse getImage(@PathVariable(value = "name") String name, HttpServletRequest request,
                                   @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "limit", required = false) Integer limit) {
        if (name == null) return SingleResponse.empty();
        if (page == null) page = 0;
        if (limit == null) limit = 5;
        String[] array = request.getRequestURI().split("/");
        _Attachment attachment = attachmentService.getByName(array[array.length - 1]);
        if (attachment == null || attachment.getPages().isEmpty())
            return SingleResponse.empty();
        if (attachment.getPageSize() < page)
            return SingleResponse.empty();
        if (page >= attachment.getPageSize())
            return SingleResponse.empty();
        if (limit >= attachment.getPageSize())
            limit = attachment.getPageSize();

        List<String> bytes = attachment.getPages().stream().skip(page).limit(limit).collect(Collectors.toList());
        return SingleResponse.of(PdfImageResponse.builder().fileName(attachment.getOriginalName()).mimeType("application/png")
                .page(page).limit(limit).total(attachment.getPageSize()).base64s(bytes).build());
    }

    //    @Cacheable(value = "image", keyGenerator = "customKeyGenerator")
    @ResponseBody
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/file/{name}", produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
    public void getImage(@PathVariable(value = "name") String name, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (name == null) return;
        String[] array = request.getRequestURI().split("/");
        _Attachment attachment = attachmentService.getByName(array[array.length - 1]);
        if (attachment != null) {
            response.setContentType(attachment.getMimeType());
            response.addHeader("Content-Disposition", String.format("attachment; filename=%s", URLEncoder.encode(attachment.getOriginalName(), StandardCharsets.UTF_8)));
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(attachment.getData());
            outputStream.flush();
            outputStream.close();
        } else
            throw new ValidatorException("Файл топилмади");
    }

    @ApiOperation(value = "Compression file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @PostMapping(value = "/compression", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void compression(HttpServletResponse response, @RequestPart(value = "file") MultipartFile file) throws IOException {
        AttachmentDto attachmentDto = pdfService.convertToPdf(Arrays.asList(file));

        response.setContentType(attachmentDto.getMimeType());
        response.addHeader("Content-Disposition", String.format("attachment; filename=%s", URLEncoder.encode(attachmentDto.getName(), StandardCharsets.UTF_8)));

        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(attachmentDto.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @ApiOperation(value = "Compression file")
    @ResponseBody
    @Transactional(propagation = Propagation.NEVER)
    @PostMapping(value = "/compression-base64", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse compression(@RequestPart(value = "file") MultipartFile file) {
        AttachmentDto attachmentDto = pdfService.convertToPdf(Arrays.asList(file));
        attachmentDto.setBytes(null);
        return SingleResponse.of(attachmentDto);
    }
}