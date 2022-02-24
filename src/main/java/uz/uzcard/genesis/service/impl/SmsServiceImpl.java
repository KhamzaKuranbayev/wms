package uz.uzcard.genesis.service.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.OtpMessageDao;
import uz.uzcard.genesis.service.SmsService;
import uz.uzcard.genesis.uitls.ApiConnector;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

@Service(value = "smsService")
public class SmsServiceImpl implements SmsService {
    private final Logger logger = LogManager.getLogger(SmsServiceImpl.class);

    @Value(value = "${service.sms.token}")
    private String smsToken;
    @Value(value = "${service.sms.url}")
    private String smsUrl;
    @Autowired
    private OtpMessageDao otpMessageDao;
    @Autowired
    private Environment environment;
    private Properties properties;

//    @Override
//    public void check(_User user) {
//        Date lastSms = otpMessageDao.lastSentCode(user);
//        if (lastSms != null && smsSendIntervalLimit > new Date().getTime() - lastSms.getTime())
//            throw new ValidatorException(smsSendIntervalLimit / 1000 / 60 + " минутдан кейин қайта уриниб кўринг");
//        if (monthlySmsLimit < otpMessageDao.countByUser(user))
//            throw new ValidatorException("ЕОПЦ администраторларига мурожаат қилинг");
//    }

    @Override
    public void sendMessage(String phone, String text) {
        if (StringUtils.isEmpty(phone))
            return;
        byte[] plainCredsBytes = smsToken.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        try {
            String response = (String) ApiConnector.newBuilder(String.class)
                    .setUrl(smsUrl)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Basic " + base64Creds)
                    .addParam("phone", phone)
                    .addParam("message", text)
                    .addParam("Ext", new Date().toString())
                    .exchange(HttpMethod.POST)
                    .build();
            if (response == null || response.isEmpty())
                throw new ValidatorException("SMS кетмади");
            logger.info("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            logger.info(response);
            logger.info("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");

            if ("Request is received".equals(response))
                return;
            try {
                Map<String, Object> map = ServerUtils.gson.fromJson(response, Map.class);
                if (map.get("error") != null) {
                    throw new ValidatorException(map.get("error").toString());
                }
            } catch (Exception e) {
                throw new RpcException(response);
            }
        } catch (ValidatorException e) {
            ServerUtils.error(logger, e);
            throw new ValidatorException(GlobalizationExtentions.localication("PHONE_NUMBER_RIGHT_ENTER"));
        }
    }

//    private String getErrorMessage(String code, String defaultMessage) {
//        if (properties == null)
//            properties = (Properties) ((StandardServletEnvironment) environment).getPropertySources().get("smsException").getSource();
//        return properties.getProperty(code) == null ? defaultMessage : properties.getProperty(code);
//    }
}