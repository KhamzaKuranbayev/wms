package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.setting.*;
import uz.uzcard.genesis.dto.api.req.user.UserFilterRequest;
import uz.uzcard.genesis.dto.api.req.user.UserItemFilterRequest;
import uz.uzcard.genesis.dto.api.req.user.UserRequest;
import uz.uzcard.genesis.dto.api.req.user.UsernameRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;

public interface UserService {

    SingleResponse save(UserRequest request);

    Boolean delete(DeleteRequest request);

    ListResponse list(UserFilterRequest request);

    Boolean sendOtpCode(UsernameRequest request);

    Boolean attachRoles(AttachRolesRequest request);

    Boolean confirm(ConfirmationCodeRequest request);

    Boolean recoverPassword(NewPasswordRequest request);

    Boolean changePassword(ChangePasswordRequest request);

    ListResponse getItems(UserItemFilterRequest request);

    SingleResponse setHashESign(HashESignRequest request);
}
