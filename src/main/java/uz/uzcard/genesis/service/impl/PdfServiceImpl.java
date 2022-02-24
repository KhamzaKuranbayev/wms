package uz.uzcard.genesis.service.impl;

import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.file.AttachmentDto;
import uz.uzcard.genesis.exception.CriticException;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.service.PdfService;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class PdfServiceImpl implements PdfService {
    public static float FACTOR = 0.5f;

    private static double getScale(double width, double height) {
        double scaleX = PageSize.A4.getWidth() / width;
        double scaleY = PageSize.A4.getHeight() / height;
        return Math.min(scaleX, scaleY);
    }

    @Override
    public AttachmentDto convertToPdf(List<MultipartFile> files) {
        try {
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();
            PdfContentByte content = writer.getDirectContent();

            for (int i = 0; i < files.size(); i++) {
                MultipartFile multipartFile = files.get(i);
                byte[] file = multipartFile.getBytes();
                String fileName = multipartFile.getOriginalFilename();
                if ("pdf".equals(FilenameUtils.getExtension(fileName.toLowerCase()))) {
                    // Read the file
                    PdfReader reader = new PdfReader(file);
                    int pageSize = reader.getNumberOfPages();

                    if (!document.isOpen()) {
                        document.open();
                    }

                    for (int number = 1; number <= pageSize; number++) {
                        PdfImportedPage page = writer.getImportedPage(reader, number);
                        document.newPage();

                        Rectangle pagesize = reader.getPageSizeWithRotation(number);
                        double oWidth = pagesize.getWidth();
                        double oHeight = pagesize.getHeight();
                        double scale = getScale(oWidth, oHeight);
                        double scaledWidth = oWidth * scale;
                        double scaledHeight = oHeight * scale;
                        int rotation = pagesize.getRotation();
                        AffineTransform transform = new AffineTransform(scale, 0, 0, scale, 0, 0);

                        switch (rotation) {
                            case 0:
                                content.addTemplate(page, transform);
                                break;
                            case 90:
                                AffineTransform rotate90 = new AffineTransform(0, -1f, 1f, 0, 0, scaledHeight);
                                rotate90.concatenate(transform);
                                content.addTemplate(page, rotate90);
                                break;
                            case 180:
                                AffineTransform rotate180 = new AffineTransform(-1f, 0, 0, -1f, scaledWidth, scaledHeight);
                                rotate180.concatenate(transform);
                                content.addTemplate(page, rotate180);
                                break;
                            case 270:
                                AffineTransform rotate270 = new AffineTransform(0, 1f, -1f, 0, scaledWidth, 0);
                                rotate270.concatenate(transform);
                                content.addTemplate(page, rotate270);
                                break;
                            default:
                                content.addTemplate(page, scale, 0, 0, scale, 0, 0);
                        }

                        //image
                    }

                    content.closePathFillStroke();
                    document.close();
                    reader.close();
                } else {
                    document.newPage();
                    com.itextpdf.text.Image image = Image.getInstance(file);
                    float width = image.getWidth();
//                    if (image.getWidth() > image.getHeight()) {
//                        image.setRotationDegrees(90);
//                        width = image.getHeight();
//                    }
                    image.setBorderWidth(0);

                    int indentation = 0;
                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - indentation) / width) * 100;
//
                    image.scalePercent(scaler);

                    document.add(image);
                }
            }

            document.close();

            if (files.isEmpty())
                throw new ValidatorException("FILE_NOT_FOUND");

            String fileName = files.get(0).getOriginalFilename();
            fileName = FilenameUtils.removeExtension(fileName) + ".pdf";
            byte[] bytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.flush();
//            bytes = manipulatePdf(fileName, bytes);

            AttachmentDto attachmentDto = new AttachmentDto(fileName, bytes, "application/pdf", bytes.length);
            splitPageImages(attachmentDto);

            return attachmentDto;
        } catch (ValidatorException | CriticException | RpcException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidatorException("Файл сақлашда хатолик бор");
        }
    }

    private void splitPageImages(AttachmentDto attachmentDto) throws IOException {
        PDDocument document = PDDocument.load(attachmentDto.getBytes());
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        attachmentDto.setSize(document.getNumberOfPages());
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = pdfRenderer.renderImage(page);
            FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
            Thumbnails.of(bim).scale(1).outputFormat("JPG").toOutputStream(outputStream);
            byte[] bytes = outputStream.getInputStream().readAllBytes();
            outputStream.close();
            attachmentDto.getPages().add(Base64.getEncoder().encodeToString(bytes));
        }
        document.close();
    }

    private byte[] manipulatePdf(String originalName, byte[] data) throws IOException, DocumentException {
        if (!"pdf".equals(FilenameUtils.getExtension(originalName.toLowerCase()))) {
            throw new ValidatorException("Фақат PDF файл ни компреслаш мумкин!!!");
        }

        // Read the file
        PdfReader reader = new PdfReader(data);

        // Look for image and manipulate image stream
        for (int i = 0; i < reader.getXrefSize(); i++) {
            PdfObject object = reader.getPdfObject(i);
            if (object == null || !object.isStream())
                continue;
            PRStream stream = (PRStream) object;
            PdfObject pdfSubtype = stream.get(PdfName.SUBTYPE);
            if (pdfSubtype != null && pdfSubtype.toString().equals(PdfName.IMAGE.toString())) {
                PdfImageObject image = new PdfImageObject(stream);
                BufferedImage bufferedImage = image.getBufferedImage();
                if (bufferedImage == null) continue;
                int width = (int) (bufferedImage.getWidth() * FACTOR);
                int height = (int) (bufferedImage.getHeight() * FACTOR);
                BufferedImage img = new BufferedImage(width > 0 ? width : 1, height > 0 ? height : 1, BufferedImage.TYPE_INT_RGB);
                AffineTransform affineTransform = AffineTransform.getScaleInstance(FACTOR, FACTOR);
                Graphics2D g = img.createGraphics();
                g.drawRenderedImage(bufferedImage, affineTransform);
                ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
                stream.clear();

                Thumbnails.of(img).size(img.getWidth(), img.getHeight()).outputFormat("JPG")/*.outputQuality(0.9)*/.toOutputStream(imgBytes);
                byte[] output = imgBytes.toByteArray();
                stream.setData(output, false, PRStream.NO_COMPRESSION);
                stream.put(PdfName.TYPE, PdfName.XOBJECT);
                stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                stream.put(PdfName.FILTER, PdfName.DCTDECODE);
                stream.put(PdfName.WIDTH, new PdfNumber(width));
                stream.put(PdfName.HEIGHT, new PdfNumber(height));
                stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
            }
        }

        // Parse Byte Array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, outputStream);
        stamper.close();
        reader.close();

        return outputStream.toByteArray();
    }
}