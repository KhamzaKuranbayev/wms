package uz.uzcard.genesis.service;

import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.hibernate.entity._Attachment;
import uz.uzcard.genesis.hibernate.entity._AttachmentView;

import java.util.List;

public interface AttachmentService {
//    _AttachmentView upload(MultipartFile file);

    _Attachment getByName(String name);

    _AttachmentView uploadPdf(List<MultipartFile> files);

    _AttachmentView uploadPdf(MultipartFile file);

    _AttachmentView delete(String name);
}
