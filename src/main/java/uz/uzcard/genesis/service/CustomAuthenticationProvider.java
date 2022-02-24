package uz.uzcard.genesis.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.hibernate.dao.UserDao;
import uz.uzcard.genesis.hibernate.entity._User;


@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDao userDao;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        _User user = userDao.getByUseName(userName);
        if (user == null) {
            throw new BadCredentialsException("Введен неверный логин или пароль");
        }
        if (!user.isActived()) {
            throw new LockedException("BLOCK_ACCOUNT");
        }

        if (StringUtils.isEmpty(authentication.getCredentials()))
            throw new BadCredentialsException("Введен неверный логин или пароль");
        String password = authentication.getCredentials().toString();
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches) {
            user.setAttempt(user.getAttempt() + 1);
            userDao.save(user);
            throw new BadCredentialsException("Введен неверный логин или пароль");
        } else {
            user.setAttempt(0);
            userDao.save(user);
        }
        return new UsernamePasswordAuthenticationToken(userName, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}