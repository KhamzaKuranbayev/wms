package uz.uzcard.genesis.service;


import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.hibernate.enums.OtpType;

import javax.validation.constraints.NotNull;
import java.util.Map;

public interface OtpMessageService {
    @Transactional
    Boolean resendOtp(String uniqueParam, Map<String, Object> model);

    void saveUserOtp(OtpType type, String generatedCode, String userName, String uniqueParam);

    Boolean checkOtp(String code, String userName);

    Long getOtpMessagesCountByUserName(Long userId);

    @Transactional
    Boolean isConfirmedForRecoveringPassword(@NotNull _User user);
}
