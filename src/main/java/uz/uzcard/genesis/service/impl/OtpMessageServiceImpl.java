package uz.uzcard.genesis.service.impl;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.OtpMessageDao;
import uz.uzcard.genesis.hibernate.dao.UserDao;
import uz.uzcard.genesis.hibernate.entity._OTPMessage;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.hibernate.enums.OtpType;
import uz.uzcard.genesis.service.OtpMessageService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

@Service(value = "oTPMessageService")
public class OtpMessageServiceImpl implements OtpMessageService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private SmsServiceImpl smsService;
    @Autowired
    private OtpMessageDao otpMessageDao;

    @Override
    @Transactional
    public Boolean resendOtp(String uniqueParam, Map<String, Object> model) {
        _OTPMessage otpMessage = otpMessageDao.findByUserName(uniqueParam);
        if (otpMessage == null) {
            throw new RpcException(GlobalizationExtentions.localication("ERROR_SENDING_OTP_CODE"));
        }
        model.put("userName", otpMessage.getUser().getUsername());
        if (OtpType.SMS.equals(otpMessage.getType())) {
            try {
//                smsService.check(otpMessage.getUser());
                smsService.sendMessage(otpMessage.getUser().getPhone(), otpMessage.getCode());
                saveUserOtp(OtpType.SMS, otpMessage.getCode(), otpMessage.getUser().getUsername(), uniqueParam);
            } catch (Exception e) {
                throw new RpcException(e.getMessage());
            }
            return true;
        }
//        } else {
//            try {
//                emailService.sendMailUser(otpMessage.getUser(), otpMessage.getCode());
//            } catch (Exception e) {
//                model.put("error", e.getMessage());
//                return model;
//            }
//        }
        DateTime dateTime = new DateTime();
        otpMessage.setExpireDate(new Date(dateTime.plusMinutes(2).getMillis()));
        otpMessageDao.save(otpMessage);
        return true;
    }

    @Override
    @Transactional
    public void saveUserOtp(OtpType type, String generatedCode, String userName, String uniqueParam) {

        if (userName != null) {
            _OTPMessage userOtp = otpMessageDao.findByUserName(userName);
            if (userOtp != null) {
                userOtp.setUpdatedDate(new Date());
                otpMessageDao.delete(userOtp);
            }
        }
        _User user = userDao.getByUseName(userName);
        _OTPMessage otpMessage = new _OTPMessage();
        otpMessage.setCode(generatedCode);
        otpMessage.setUser(user);
        otpMessage.setType(type);
        otpMessage.setUniqueParam(uniqueParam);
        otpMessage.setCreatedDate(new Date());
        DateTime dateTime = new DateTime();
        otpMessage.setExpireDate(new Date(dateTime.plusMinutes(2).getMillis()));
        otpMessage.setMessage(generatedCode);
        otpMessageDao.save(otpMessage);
    }

    @Override
    @Transactional
    public Boolean checkOtp(String code, String userName) {
        if (userName != null) {
            _OTPMessage userOtp = otpMessageDao.findByUserName(userName);
            if (userOtp != null) {
                if (code != null && code.equals(userOtp.getCode())) {
                    if (userOtp.getExpireDate().after(new Date())) {
                    } else {
                        throw new ValidatorException(GlobalizationExtentions.localication("CONFIRMATION_CODE_IS_EXPIRED"));
                    }
                } else {
                    throw new ValidatorException(GlobalizationExtentions.localication("CONFIRMATION_CODE_IS_INCORRECT"));
                }
                userOtp.setUpdatedDate(new Date());
                userOtp.setState(_State.CONFIRMED);
                otpMessageDao.save(userOtp);
                return true;
            }
        }
        return false;
    }

    @Override
    public Long getOtpMessagesCountByUserName(Long userId) {
        Date before = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        Date current = new Date(System.currentTimeMillis());
        return otpMessageDao.getAllMessagesByUserName(userId, before, current);
    }

    @Override
    @Transactional
    public Boolean isConfirmedForRecoveringPassword(@NotNull _User user) {
        _OTPMessage userOtp = otpMessageDao.findByUserName(user.getUsername());
        if (userOtp != null) {
            if (userOtp.getUpdatedDate() != null) {
                if (userOtp.getUpdatedDate().toInstant().plusSeconds(60).isAfter(new Date().toInstant())) {
                    if (_State.CONFIRMED.equals(userOtp.getState())) {
                        return true;
                    } else throw new ValidatorException(GlobalizationExtentions.localication("PASSWORD_CHANGE_ACCESS"));
                } else throw new ValidatorException("Recoverying password is fineshed, try from the beginning");

            }
        }
        return false;
    }
}
