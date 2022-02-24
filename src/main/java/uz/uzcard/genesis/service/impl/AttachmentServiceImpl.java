package uz.uzcard.genesis.service.impl;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import uz.uzcard.genesis.dto.file.AttachmentDto;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.AttachmentDao;
import uz.uzcard.genesis.hibernate.entity._Attachment;
import uz.uzcard.genesis.hibernate.entity._AttachmentView;
import uz.uzcard.genesis.service.AttachmentService;
import uz.uzcard.genesis.service.PdfService;
import uz.uzcard.genesis.uitls.AttachmentUtils;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class AttachmentServiceImpl implements AttachmentService {
    @Autowired
    private AttachmentDao attachmentDao;
    @Autowired
    private PdfService pdfService;

//    @Override
//    public _AttachmentView upload(MultipartFile file) {
//        try {
//            if (file == null || file.isEmpty() || file.getBytes().length < 1)
//                throw new ValidatorException(GlobalizationExtentions.localication("FILE_REQUIRED"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        _Attachment attachment = new _Attachment();
//        try {
//            attachment.setData(file.getBytes());
//        } catch (IOException e) {
////            e.printStackTrace();
//            throw new RpcException(GlobalizationExtentions.localication("ERROR_WHILE_SAVING_FILE"));
//        }
//        String name = AttachmentUtils.generateName(file.getOriginalFilename());
//
//        attachment.setFileSize(file.getSize());
//        attachment.setMimeType(file.getContentType());
//        attachment.setOriginalName(file.getOriginalFilename());
//        attachment.setName(name);
//        attachmentDao.save(attachment);
//        return attachmentDao.getById(attachment.getId());
//    }

    @Override
    public _Attachment getByName(String name) {
        return attachmentDao.getByName(name);
    }

    @Override
    public _AttachmentView uploadPdf(List<MultipartFile> files) {
        AttachmentDto attachmentDto = pdfService.convertToPdf(files);

        if (attachmentDto == null || attachmentDto.isEmpty() || attachmentDto.getBytes().length < 1)
            throw new ValidatorException(GlobalizationExtentions.localication("FILE_REQUIRED"));

        _Attachment attachment = new _Attachment();
        attachment.setData(attachmentDto.getBytes());
        String name = AttachmentUtils.generateName(attachmentDto.getName());

        attachment.setFileSize(attachmentDto.getSize());
        attachment.setMimeType(attachmentDto.getMimeType());
        attachment.setOriginalName(attachmentDto.getName());
        attachment.setName(name);
        attachment.setPages(attachmentDto.getPages());
        attachment.setPageSize(attachment.getPages().size());
        attachmentDao.save(attachment);
        return attachmentDao.getById(attachment.getId());
    }

    @Override
    public _AttachmentView uploadPdf(MultipartFile file) {
        return uploadPdf(List.of(file));
    }

    @Override
    public _AttachmentView delete(String name) {
        _Attachment attachment = attachmentDao.getByName(name);
        attachmentDao.delete(attachment);
        return attachmentDao.getById(attachment.getId());
    }

    // Nothing here should throw IOException in reality - work out what you want to do.
    public byte[] convertStream(CommonsMultipartFile file) throws IOException {
        ByteArrayInputStream original = new ByteArrayInputStream(file.getBytes());
        DiskFileItem fileItem = (DiskFileItem) file.getFileItem();
        InputStreamReader contentReader = new InputStreamReader(original, fileItem.getDefaultCharset());

        int readCount;
        char[] buffer = new char[4096];
        try (ByteArrayOutputStream converted = new ByteArrayOutputStream()) {
            try (Writer writer = new OutputStreamWriter(converted, StandardCharsets.UTF_8)) {
                while ((readCount = contentReader.read(buffer, 0, buffer.length)) != -1) {
                    writer.write(buffer, 0, readCount);
                }
            }
            return converted.toByteArray();
        }
    }
}
