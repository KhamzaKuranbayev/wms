package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.setting.*;
import uz.uzcard.genesis.dto.api.req.user.UserFilterRequest;
import uz.uzcard.genesis.dto.api.req.user.UserItemFilterRequest;
import uz.uzcard.genesis.dto.api.req.user.UserRequest;
import uz.uzcard.genesis.dto.api.req.user.UsernameRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.DepartmentDao;
import uz.uzcard.genesis.hibernate.dao.RoleDao;
import uz.uzcard.genesis.hibernate.dao.UserDao;
import uz.uzcard.genesis.hibernate.dao.UserHashESignDao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._Role;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.hibernate.entity._UserHashESign;
import uz.uzcard.genesis.hibernate.enums.OtpType;
import uz.uzcard.genesis.service.AttachmentService;
import uz.uzcard.genesis.service.OtpMessageService;
import uz.uzcard.genesis.service.SmsService;
import uz.uzcard.genesis.service.UserService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final DepartmentDao departmentDao;
    private final SmsService smsService;
    private final OtpMessageService otpMessageService;
    private final AttachmentService attachmentService;
    private final UserHashESignDao userHashESignDao;

    @Autowired
    public UserServiceImpl(UserDao userDao, RoleDao roleDao, DepartmentDao departmentDao, SmsService smsService, OtpMessageService otpMessageService, AttachmentService attachmentService, UserHashESignDao userHashESignDao) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.departmentDao = departmentDao;
        this.smsService = smsService;
        this.otpMessageService = otpMessageService;
        this.attachmentService = attachmentService;
        this.userHashESignDao = userHashESignDao;
    }

    @Override
    public SingleResponse save(UserRequest request) {
        _User user;
        if (ServerUtils.isEmpty(request)) {
            throw new ValidatorException("REQUEST_IS_NULL");
        }
        if (request.getId() == null && !request.isForCurrentUser()) {
            user = new _User();
            _User validationUser = userDao.getByUseName(request.getUserName());
            if (validationUser != null) {
                throw new ValidatorException(GlobalizationExtentions.localication("USER_NAME_HAS_ALREADY"));
            }
            if (!StringUtils.isEmpty(request.getPhone())) {
                validationUser = userDao.getByPhoneNumber(request.getPhone());
                if (validationUser != null) {
                    throw new ValidatorException(GlobalizationExtentions.localication("PHONE_NUMBER_HAS_ALREADY"));
                }
            }
            if (!StringUtils.isEmpty(request.getEmail())) {
                validationUser = userDao.getByEmail(request.getEmail());
                if (validationUser != null) {
                    throw new ValidatorException(GlobalizationExtentions.localication("EMAIL_HAS_ALREADY"));
                }
            }

            if (!StringUtils.isEmpty(request.getDepartmentId())) {
                _Department department = departmentDao.get(request.getDepartmentId());
                if (ServerUtils.isEmpty(department)) {
                    throw new ValidatorException(GlobalizationExtentions.localication("DEPARTMENT_NOT_FOUND"));
                } else user.setDepartment(department);
            }

            user.setUserName(request.getUserName());
            user.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setMiddleName(request.getMiddleName());
            user.setLeader(request.isLeader());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setActived(true);
            if (request.getRoles() != null) {
                Stream<_Role> roleStream = roleDao.findByIds(request.getRoles());
                user.setRoles(roleStream.collect(Collectors.toList()));
            }

        } else {
            if (request.isForCurrentUser()) {
                user = SessionUtils.getInstance().getUser();
            } else {
                user = userDao.get(request.getId());
                if (user == null)
                    throw new ValidatorException(GlobalizationExtentions.localication("USER_NOT_REGISTERED"));
                if (!StringUtils.isEmpty(request.getDepartmentId())) {
                    _Department department = departmentDao.get(request.getDepartmentId());
                    if (ServerUtils.isEmpty(department)) {
                        throw new ValidatorException(GlobalizationExtentions.localication("DEPARTMENT_NOT_FOUND"));
                    }
                    user.setDepartment(department);
                }
            }
            if (!StringUtils.isEmpty(request.getUserName())) {
                _User byUseName = userDao.getByUseNameWithOutId(request.getUserName(), user.getId());
                if (byUseName != null)
                    throw new ValidatorException(GlobalizationExtentions.localication("USER_NAME_HAS_ALREADY"));

                user.setUserName(request.getUserName());
            }

            if (!StringUtils.isEmpty(request.getEmail()))
                user.setEmail(request.getEmail());
            if (!StringUtils.isEmpty(request.getPhone()))
                user.setPhone(request.getPhone());
            if (!StringUtils.isEmpty(request.getFirstName()))
                user.setFirstName(request.getFirstName());
            if (!StringUtils.isEmpty(request.getLastName()))
                user.setLastName(request.getLastName());
            if (!StringUtils.isEmpty(request.getMiddleName()))
                user.setMiddleName(request.getMiddleName());
            user.setLeader(request.isLeader());
            if (!StringUtils.isEmpty(request.getPassword()))
                user.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
            if (request.getRoles() != null) {
                Stream<_Role> roleStream = roleDao.findByIds(request.getRoles());
                user.setRoles(roleStream.collect(Collectors.toList()));
            }
        }

        user = userDao.save(user);
        return SingleResponse.of(user, (user1, map) -> {
            List<Long> nameRoles = user1.getRoles().stream().map(role -> {
                return role.getId();
            }).collect(Collectors.toList());
            map.addStrings("roles", nameRoles);
            if (user1.getDepartment() != null) {
                map.add("departmentId", user1.getDepartment().getId());
                map.add("departmentName", user1.getDepartment().getNameByLanguage());
            }
            return map;
        });
    }

    @Override
    public Boolean delete(DeleteRequest request) {
        if (ServerUtils.isEmpty(request.getObjectId())) {
            throw new RpcException(GlobalizationExtentions.localication("ID_REQUIRED"));
        }
        _User user = userDao.get(request.getObjectId());
        if (ServerUtils.isEmpty(user)) {
            throw new RpcException(GlobalizationExtentions.localication("USER_NOT_FOUND"));
        }
        user.setActived(false);
        user.setState("DELETED");
        userDao.save(user);
        return true;
    }

    @Override
    public ListResponse list(UserFilterRequest request) {
        FilterParameters filter = new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            setSortColumn(request.getSortBy());
            setSortType(request.getSortDirection());
            if (request.getRoles() != null) {
                addStrings("roles", request.getRoles());
            }
            if (request.getDepartmentId() != null) {
                addLong("departmentId", request.getDepartmentId());
            }
            if (request.isDepartment()) {
                addString("isDepartment", "true");
            }
            if (!ServerUtils.isEmpty(request.getEmail())) {
                addString("email", request.getEmail());
            }
            if (!ServerUtils.isEmpty(request.getUserName())) {
                addString("userName", request.getUserName());
            }
            if (!ServerUtils.isEmpty(request.getFirstName())) {
                addString("firstName", request.getFirstName());
            }
            if (!ServerUtils.isEmpty(request.getLastName())) {
                addString("lastName", request.getLastName());
            }
            if (!ServerUtils.isEmpty(request.getPhoneNumber())) {
                addString("phoneNumber", request.getPhoneNumber());
            }
            if (!ServerUtils.isEmpty(request.getDepType())) {
                addString("depType", request.getDepType().name());
            }
        }};
        Integer total = userDao.total(filter);
        return ListResponse.of(userDao.list(filter), total, ((user, map) -> {
            List<Long> nameRoles = user.getRoles().stream().map(role -> {
                return role.getId();
            }).collect(Collectors.toList());
            map.addStrings("roles", nameRoles);
            if (user.getDepartment() != null) {
                map.add("departmentId", user.getDepartment().getId());
                map.add("departmentName", user.getDepartment().getNameByLanguage());
            }
            return map;
        }));
    }

    @Override
    public Boolean sendOtpCode(UsernameRequest request) {
        if (ServerUtils.isEmpty(request.getUsername())) {
            throw new ValidatorException(GlobalizationExtentions.localication("USERNAME_REQUIRED"));
        }
        _User user = userDao.getByUseName(request.getUsername());
        if (ServerUtils.isEmpty(user)) {
//            throw new ValidatorException(String.format("No users found with this username '%s'", request.getUsername()));
            throw new ValidatorException(String.format(GlobalizationExtentions.localication("USER_NOT_FOUND_WITH_USERNAME"), request.getUsername()));
        }
        if (!ServerUtils.isEmpty(user.getPhone())) {
            String generatedCode = ServerUtils.generateRandomCode();

//            smsService.check(user);
            Long otpMessagesCount = otpMessageService.getOtpMessagesCountByUserName(user.getId());
            if (otpMessagesCount <= 5) {
                smsService.sendMessage(user.getPhone(), "Your Verification Code: " + generatedCode);
                otpMessageService.saveUserOtp(OtpType.SMS, generatedCode, user.getUsername(), user.getPhone());
            } else {
                throw new ValidatorException("OUT_OF_LIMIT");
            }
        } else
            throw new ValidatorException("This user has no phone number");
        return true;
    }

    @Override
    public Boolean attachRoles(AttachRolesRequest request) {
        if (ServerUtils.isEmpty(request)) {
            throw new ValidatorException("REQUEST_IS_NULL");
        }
        if (ServerUtils.isEmpty(request.getUserId())) {
            throw new ValidatorException(GlobalizationExtentions.localication("ID_REQUIRED"));
        }
        _User user = userDao.get(request.getUserId());
        if (ServerUtils.isEmpty(user)) {
            throw new ValidatorException(GlobalizationExtentions.localication("USER_NOT_FOUND"));
        } else {
            user.setRoles(roleDao.findByIds(request.getRoles()).collect(Collectors.toList()));
            userDao.save(user);
        }
        return true;
    }

    @Override
    public Boolean confirm(ConfirmationCodeRequest request) {
        if (ServerUtils.isEmpty(request.getCode())) {
            throw new ValidatorException(GlobalizationExtentions.localication("CONFIRMATION_CODE_REQUIRED"));
        }
        if (ServerUtils.isEmpty(request.getUsername())) {
            throw new ValidatorException(GlobalizationExtentions.localication("USERNAME_REQUIRED"));
        }
        _User user = userDao.getByUseName(request.getUsername());
        if (ServerUtils.isEmpty(user)) {
            throw new ValidatorException(String.format(GlobalizationExtentions.localication("USER_NOT_FOUND_WITH_USERNAME"), request.getUsername()));
        }
        return otpMessageService.checkOtp(request.getCode(), request.getUsername());
    }

    @Override
    public Boolean recoverPassword(NewPasswordRequest request) {
        if (ServerUtils.isEmpty(request.getNewPassword())) {
            throw new RpcException(GlobalizationExtentions.localication("NEW_PASSWORD_REQUIRED"));
        }
        if (ServerUtils.isEmpty(request.getUsername())) {
            throw new RpcException(GlobalizationExtentions.localication("USERNAME_REQUIRED"));
        }
        _User user = userDao.getByUseName(request.getUsername());
        if (ServerUtils.isEmpty(user)) {
            throw new ValidatorException(String.format(GlobalizationExtentions.localication("USER_NOT_FOUND_WITH_USERNAME"), request.getUsername()));
        }
        if (!otpMessageService.isConfirmedForRecoveringPassword(user))
            throw new ValidatorException(" You have no access to recover your password! ");

        user.setPassword(new BCryptPasswordEncoder().encode(request.getNewPassword()));
        userDao.save(user);
        return true;
    }

    @Override
    public Boolean changePassword(ChangePasswordRequest request) {

        if (ServerUtils.isEmpty(request.getUsername())) {
            throw new RpcException(GlobalizationExtentions.localication("USERNAME_REQUIRED"));
        }

        if (ServerUtils.isEmpty(request.getOldPassword())) {
            throw new RpcException(GlobalizationExtentions.localication("OLD_PASSWORD_REQUIRED"));
        }

        if (ServerUtils.isEmpty(request.getNewPassword())) {
            throw new RpcException(GlobalizationExtentions.localication("NEW_PASSWORD_REQUIRED"));
        }

        _User user = userDao.getByUseName(request.getUsername());
        if (ServerUtils.isEmpty(user)) {
            throw new ValidatorException(String.format(GlobalizationExtentions.localication("USER_NOT_FOUND_WITH_USERNAME"), request.getUsername()));
        }

        if (!(new BCryptPasswordEncoder().matches(request.getOldPassword(), user.getPassword()))) {
            throw new ValidatorException(GlobalizationExtentions.localication("OLD_PASSWORD_INCORRECT"));
        }
        user.setPassword(new BCryptPasswordEncoder().encode(request.getNewPassword()));
        userDao.save(user);
        return true;
    }

    @Override
    public ListResponse getItems(UserItemFilterRequest request) {
        FilterParameters filter = new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            setName(request.getName());
        }};
        Integer total = userDao.total(filter);
        Stream<_User> list = userDao.list(filter);

        return ListResponse.of(list.map(user -> new SelectItem(user.getId(), user.getShortName(), "" + user.getId()))
                        .collect(Collectors.toList()),
                total);
    }

    @Override
    public SingleResponse setHashESign(HashESignRequest request) {
        _User user = SessionUtils.getInstance().getUser();
        if (user == null) {
            throw new ValidatorException("USER_NOT_REGISTERED");
        }
        _UserHashESign userHashESign = new _UserHashESign();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        userHashESign.setToHashESignDate(calendar.getTime());
        userHashESign.setHashESign(request.getHashESign());
        userHashESign.setUser(user);
        userHashESign = userHashESignDao.save(userHashESign);
        return SingleResponse.of(userHashESign, (userHashESign1, map) -> map);
    }
}
