package uz.uzcard.genesis.service;

import com.itextpdf.text.DocumentException;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.file.AttachmentDto;

import java.io.IOException;
import java.util.List;

public interface PdfService {
    AttachmentDto convertToPdf(List<MultipartFile> files);
}
