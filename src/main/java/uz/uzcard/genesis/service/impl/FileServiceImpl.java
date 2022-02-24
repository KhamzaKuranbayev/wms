//package uz.uzcard.genesis.service.impl;
//
//import com.itextpdf.text.Document;
//import com.itextpdf.text.Image;
//import com.itextpdf.text.PageSize;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfWriter;
//import lombok.SneakyThrows;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import uz.uzcard.genesis.dto.file.AttachmentDto;
//import uz.uzcard.genesis.hibernate.entity._AttachmentView;
//import uz.uzcard.genesis.service.AttachmentService;
//import uz.uzcard.genesis.service.FileService;
//import uz.uzcard.genesis.service.PdfService;
//
//import java.io.ByteArrayOutputStream;
//import java.util.List;
//
//@Service
//public class FileServiceImpl implements FileService {
//
//    /**
//     * The multiplication factor for the image.
//     */
//    public static float FACTOR = 1f;
//
//    @Autowired
//    private AttachmentService attachmentService;
//
//    @Autowired
//    private PdfService pdfService;
//
//    @SneakyThrows
//    @Override
//    public _AttachmentView manipulatePdf(List<MultipartFile> files) {
//        AttachmentDto attachmentDto = pdfService.convertToPdf(files);
//        _AttachmentView upload = attachmentService.upload(attachmentDto);
//        return upload;
//
////        // Default OutputStream for write pdf files
////        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
////
////        //Create document and pdfReader objects.
////        Document document = new Document();
////        List<PdfReader> readers = new ArrayList<PdfReader>();
////        int totalPages = 0;
////
////        //Create pdf Iterator object using inputPdfList.
////        Iterator<MultipartFile> multipartFileIterator = files.iterator();
////
////        // Create reader list for the input pdf files.
////        while (multipartFileIterator.hasNext()) {
////            MultipartFile multipartFile = multipartFileIterator.next();
////            PdfReader pdfReader;
////
////            if (!"pdf".equals(FilenameUtils.getExtension(multipartFile.getOriginalFilename())))
////                pdfReader = new PdfReader(imageToPdf(multipartFile));
////            else
////                pdfReader = new PdfReader(multipartFile.getBytes());
////
////            readers.add(pdfReader);
////            totalPages = totalPages + pdfReader.getNumberOfPages();
////        }
////
////        // Create writer for the outputStream
////        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
////
////        //Open document.
////        document.open();
////
////        //Contain the pdf data.
////        PdfContentByte pageContentByte = writer.getDirectContent();
////
////        PdfImportedPage pdfImportedPage;
////        int currentPdfReaderPage = 1;
////        Iterator<PdfReader> iteratorPDFReader = readers.iterator();
////
////        // Iterate and process the reader list.
////        while (iteratorPDFReader.hasNext()) {
////            PdfReader pdfReader = iteratorPDFReader.next();
////            //Create page and add content.
////            while (currentPdfReaderPage <= pdfReader.getNumberOfPages()) {
////                document.newPage();
////                pdfImportedPage = writer.getImportedPage(
////                        pdfReader, currentPdfReaderPage);
////                pageContentByte.addTemplate(pdfImportedPage, 0, 0);
////                currentPdfReaderPage++;
////            }
////            currentPdfReaderPage = 1;
////        }
////
////        //Close document and outputStream.
////        outputStream.flush();
////        document.close();
////        outputStream.close();
////
////        DiskFileItem dfi = new DiskFileItem("FileData", "application/pdf", true,
////                "DefaultOrderFile", 100000000, null);
////
////        try {
////            dfi.getOutputStream().write(outputStream.toByteArray());
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        CommonsMultipartFile multipartFile = new CommonsMultipartFile(dfi);
////
////        _AttachmentView upload = attachmentService.upload(multipartFile);
////
////        return upload;
//    }
//
//    @SneakyThrows
//    public byte[] imageToPdf(MultipartFile multipartFile) {
//        Document document = new Document(PageSize.A4);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
//        document.open();
//        PdfContentByte content = writer.getDirectContent();
//
//        document.newPage();
//        Image image = Image.getInstance(multipartFile.getBytes());
//        float width = image.getWidth();
//        if (image.getWidth() > image.getHeight()) {
//            image.setRotationDegrees(90);
//            width = image.getHeight();
//        }
//        image.setAbsolutePosition(0, 0);
//        image.setBorderWidth(0);
//
//        int indentation = 0;
//        float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - indentation) / width) * 100;
//
//        image.scalePercent(scaler);
//
//        document.add(image);
//        document.close();
//        return byteArrayOutputStream.toByteArray();
//    }
//}
