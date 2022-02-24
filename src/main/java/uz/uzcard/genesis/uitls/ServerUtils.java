package uz.uzcard.genesis.uitls;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uz.uzcard.genesis.config.ApplicationContextProvider;
import uz.uzcard.genesis.dto.api.req.ESignature.CertificateDto;
import uz.uzcard.genesis.dto.api.req.ESignature.Pkcs7InfoDto;
import uz.uzcard.genesis.dto.api.req.ESignature.ResponseDto;
import uz.uzcard.genesis.dto.api.req.ESignature.SignersDto;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.UserHashESignDao;
import uz.uzcard.genesis.hibernate.entity._UserHashESign;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ServerUtils {
    public static final String[] alphabet = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z".split(" ");
    private static final String HASH_EXCEPTION = "HASH_ESIGN_EXPERIED_DATE_EXCEPTION";

    @Value("${oauth2.clientId}")
    private static String signUrl;
    public static PropertyNamingStrategy.UpperCamelCaseStrategy upperCamelCase = new PropertyNamingStrategy.UpperCamelCaseStrategy();
    public static Gson gson = new GsonBuilder().serializeNulls().setLenient().create();
    public static PropertyNamingStrategy.PropertyNamingStrategyBase camelCase = new PropertyNamingStrategy.PropertyNamingStrategyBase() {
        @Override
        public String translate(String s) {
            return s.substring(0, 1).toLowerCase() + s.substring(1);
        }
    };
    public static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String TIME_FORMAT = "H : mm";// e.g 20180616;
    public static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern(TIME_FORMAT);
    private static final String SHORT_DATE_FORMAT = "dd-MM-yyyy";// e.g 20180616;
    private static final String DD_MM_FORMAT = "dd - MMMM";
    private static final SimpleDateFormat shortDateFormat = new SimpleDateFormat(SHORT_DATE_FORMAT);
    private static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final SimpleDateFormat fullDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ddMMFormat = DateTimeFormatter.ofPattern(DD_MM_FORMAT);
    private static final SimpleDateFormat uniqueDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
    private static ObjectMapper objectMapper;

    public static String numericFormat(Object number) {
        return NumberFormat.getInstance(new Locale("ru")).format(Double.parseDouble("" + number));
    }

    public static DateTimeFormatter getTimeFormat() {
        return timeFormat;
    }

    private static SimpleDateFormat getShortDateFormat() {
        return shortDateFormat;
    }

    public static Date getFullDateFormat(String date) {
        try {
            return fullDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date getFullDateFormat2(String date) {
        try {
            return fullDateFormat2.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DateTimeFormatter getddMMFormat() {
        return ddMMFormat;
    }

    public static ZonedDateTime toZonedDateTime(Date utilDate) {
        if (utilDate == null) {
            return null;
        }
        final ZoneId systemDefault = ZoneId.systemDefault();
        return ZonedDateTime.ofInstant(utilDate.toInstant(), systemDefault);
    }

    public static String getTimeFormat(LocalTime date) {
        if (date == null) return null;
        return date.format(getTimeFormat());
    }

    public static String getShortDateFormat(Date date) {
        if (date == null) return null;
        return getShortDateFormat().format(date);
    }

    public static String getShortDateFormat(Instant date) {
        if (date == null) return null;
        return getShortDateFormat().format(Date.from(date));
    }

    public static String getddMMFormat(LocalDate date) {
        if (date == null) return null;
        return getddMMFormat().format(date);
    }

    public static String generateUniqueCode() {
        return uniqueDateFormat.format(new Date());
    }

    public static String generateRandomCode() {
        Random random = new Random();
        int generatedCode = random.nextInt(900000) + 100000;
        return String.valueOf(generatedCode);
    }

    public static LocalTime getTimeParse(String date) {
        try {
            if (date == null || "".equals(date)) return null;
            return LocalTime.parse(date, getTimeFormat());
        } catch (Exception e) {
            throw new RpcException("ENTER_TIME_FORMAT_CORRECTLY");
        }
    }

    public static JsonNode fromStringToNode(String data) {
        try {
            return objectMapper.readTree(data);
        } catch (IOException e) {
            throw new RuntimeException("could not parse string data");
        }
    }

    public static String jsonParserIgnoreNull(Object object) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(object);
    }

    public static <T> T fromStringToNodes(String data, TypeReference typeReference) {
        try {
            return (T) objectMapper.readValue(data, typeReference);
        } catch (Exception e) {

            throw new RuntimeException(String.format("could not parse string data %s", data));
        }
    }


    public static byte[] getQRCodeImage(String text, Integer width, Integer height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "png", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void error(Logger log, Exception e) {
    }

    public static String encodeToBase64(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isEmpty(List<?> items) {
        return items == null || items.isEmpty();
    }

    public static boolean isEmpty(Object l) {
        return l == null;
    }

    public static void checkESign(String key) {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = null;
        try {
            uri = new URI("http://localhost:55555/api/eimzo/VerifyPkcs");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Map<String, String> data = new HashMap<>();
        data.put("sign", key);
        ResponseEntity<String> result = restTemplate.postForEntity(uri, data, String.class);

        ResponseDto jsonResponse = null;
        jsonResponse = ServerUtils.gson.fromJson(result.getBody().toString(), ResponseDto.class);
        if (jsonResponse != null && jsonResponse.isSuccess()) {
            Pkcs7InfoDto pkcs7Info = jsonResponse.getPkcs7Info();
            if (pkcs7Info == null) {
                throw new BadCredentialsException("Маьлумот топилмади!");
            }
            if (pkcs7Info.getSigners().size() < 1)
                throw new BadCredentialsException("Парол хато киритилган");

            SignersDto signer = pkcs7Info.getSigners().get(0);
            if (!signer.isVerified())
                throw new BadCredentialsException("Имзо мос тушмади");
            if (!signer.isCertificateVerified())
                throw new BadCredentialsException("Сертификат мос тушмади");
            List<CertificateDto> certs = signer.getCertificate();
            if (certs == null || certs.size() < 1)
                throw new BadCredentialsException("Сертификат яроқсиз");
//            byte[] documentByte = Base64.getDecoder().decode(jsonResponse.getPkcs7Info().getDocumentBase64());
//            String dataJson = new String(documentByte, StandardCharsets.UTF_8);
            String tin = null;
            if (certs.get(0).getSubjectName() != null) {
                for (String split : certs.get(0).getSubjectName().split(",")) {
                    String[] x = split.split("=");
                    if ("UID".equals(x[0])) {
                        tin = x[1];
                        break;
                    }
                }
            }
        }
    }

    public static String getAlphabetCode(Integer index) {
        String code = alphabet[index % alphabet.length];
        if (index >= alphabet.length) {
            code = code + (index / alphabet.length);
        }
        return code;
    }

    public static void checkUserHashESign() {
        if (SessionUtils.getInstance().getUser().getDepartment().getDepType() != null) {
            if (OrderClassification.DEPARTMENT.equals(SessionUtils.getInstance().getUser().getDepartment().getDepType())) {
                return;
            }
        } else {
            throw new ValidatorException("Бўлим тури топилмади");
        }
        Long id = SessionUtils.getInstance().getUser().getId();
        if (id == null) {
            throw new ValidatorException("USER_NOT_REGISTERED");
        }
        UserHashESignDao hashESignDao = ApplicationContextProvider.applicationContext.getBean(UserHashESignDao.class);
        _UserHashESign lastOne = hashESignDao.getLastOne(id, new Date());
        if (lastOne == null) {
            throw new RpcException(HASH_EXCEPTION);
        }
    }
}