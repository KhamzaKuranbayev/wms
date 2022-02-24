package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.setting.*;
import uz.uzcard.genesis.dto.api.req.user.UserFilterRequest;
import uz.uzcard.genesis.dto.api.req.user.UserItemFilterRequest;
import uz.uzcard.genesis.dto.api.req.user.UserRequest;
import uz.uzcard.genesis.dto.api.req.user.UsernameRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.service.UserService;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.Collection;
import java.util.stream.Collectors;

@Api(value = "User controller", description = "User list")
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "User list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(UserFilterRequest request) {
        return userService.list(request);
    }

    @ApiOperation(value = "User items")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse items(UserItemFilterRequest request) {
        return userService.getItems(request);
    }

    @ApiOperation(value = "User save")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody UserRequest request) {
        return userService.save(request);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get current user info")
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getCurrentUserInfo() {
        _User user = SessionUtils.getInstance().getUser();
        if (user == null)
            throw new BadCredentialsException("UNAUTHORIZED");
        return SingleResponse.of(user, (user1, map) -> {
            map.remove("password");
            map.remove("id");
            map.setId(null);
            map.add("shortName", user.getShortName());
            map.addStrings("roles", SessionUtils.getInstance().getRoles());

            if (SecurityContextHolder.getContext().getAuthentication() != null &&
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities() != null)
                map.addStrings("permission",
                        ((Collection<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                                .stream().map(grantedAuthority -> grantedAuthority.getAuthority()).collect(Collectors.toList()));
            return map;
        });
    }

    @ApiOperation(value = "User delete")
    @Transactional
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(DeleteRequest request) {
        return SingleResponse.of(userService.delete(request));
    }

    @ApiOperation(value = "Send OTP code for recovering password")
    @Transactional
    @PostMapping(value = "/password-recovery/send-otp-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse sendOtp(UsernameRequest request) {
        return SingleResponse.of(userService.sendOtpCode(request));
    }

    @ApiOperation(value = "Confirmation Code")
    @Transactional
    @PostMapping(value = "/password-recovery/confirm-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse confirm(@RequestBody ConfirmationCodeRequest request) {
        return SingleResponse.of(userService.confirm(request));
    }

    @ApiOperation(value = "Password Recovery")
    @Transactional
    @PostMapping(value = "/password-recovery", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse recoverPassword(@RequestBody NewPasswordRequest request) {
        return SingleResponse.of(userService.recoverPassword(request));
    }

    @ApiOperation(value = "Change Password")
    @Transactional
    @PostMapping(value = "/password-change", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse changePassword(@RequestBody ChangePasswordRequest request) {
        return SingleResponse.of(userService.changePassword(request));
    }

    @ApiOperation(value = "Attach Roles")
    @Transactional
    @PostMapping(value = "attach-role", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse attachRoles(@RequestBody AttachRolesRequest request) {
        return SingleResponse.of(userService.attachRoles(request));
    }

    @ApiOperation(value = "Auth or expired Date (Set Hash ESign)")
    @Transactional
    @PostMapping(value = "/set-hash-e-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse setHashESign(@RequestBody HashESignRequest request) {
        return userService.setHashESign(request);
    }
}
