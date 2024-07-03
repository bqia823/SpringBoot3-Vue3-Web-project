package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling account information
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    // Cool down the time limit for sending verification emails, in seconds
    @Value("${spring.web.verify.mail-limit}")
    int verifyLimit;

    @Resource
    AmqpTemplate rabbitTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    FlowUtils flow;

    /**
     * Finds user details by username or email from the database
     * @param username the username
     * @return user details
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if(account == null)
            throw new UsernameNotFoundException("Incorrect username or password");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    /**
     * Generates a registration verification code, stores it in Redis, and submits an email send request to the message queue
     * @param type the type
     * @param email the email address
     * @param address the request IP address
     * @return the result of the operation, null if normal, otherwise the reason for the error
     */
    public String registerEmailVerifyCode(String type, String email, String address){
        synchronized (address.intern()) {
            if(!this.verifyLimit(address))
                return "Too many requests, please try again later";
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = Map.of("type",type,"email", email, "code", code);
            rabbitTemplate.convertAndSend(Const.MQ_MAIL, data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }
    }

    /**
     * Registers an account using an email verification code, checking if the code is correct and if the email or username is already taken
     * @param info registration information
     * @return the result of the operation, null if normal, otherwise the reason for the error
     */
    public String registerEmailAccount(EmailRegisterVO info){
        String email = info.getEmail();
        String code = this.getEmailVerifyCode(email);
        if(code == null) return "Please get the verification code first";
        if(!code.equals(info.getCode())) return "Incorrect verification code, please re-enter";
        if(this.existsAccountByEmail(email)) return "This email address is already registered";
        String username = info.getUsername();
        if(this.existsAccountByUsername(username)) return "This username is already taken, please choose another";
        String password = passwordEncoder.encode(info.getPassword());
        Account account = new Account(null, info.getUsername(),
                password, email, Const.ROLE_DEFAULT, new Date());
        if(!this.save(account)) {
            return "Internal error, registration failed";
        } else {
            this.deleteEmailVerifyCode(email);
            return null;
        }
    }

    /**
     * Resets the password using an email verification code, checking if the code is correct
     * @param info reset information
     * @return the result of the operation, null if normal, otherwise the reason for the error
     */
    @Override
    public String resetEmailAccountPassword(EmailResetVO info) {
        String verify = resetConfirm(new ConfirmResetVO(info.getEmail(), info.getCode()));
        if(verify != null) return verify;
        String email = info.getEmail();
        String password = passwordEncoder.encode(info.getPassword());
        boolean update = this.update().eq("email", email).set("password", password).update();
        if(update) {
            this.deleteEmailVerifyCode(email);
        }
        return update ? null : "Update failed, please contact the administrator";
    }

    /**
     * Confirms the password reset by verifying the verification code
     * @param info verification information
     * @return the result of the operation, null if normal, otherwise the reason for the error
     */
    @Override
    public String resetConfirm(ConfirmResetVO info) {
        String email = info.getEmail();
        String code = this.getEmailVerifyCode(email);
        if(code == null) return "Please get the verification code first";
        if(!code.equals(info.getCode())) return "Incorrect verification code, please re-enter";
        return null;
    }

    /**
     * Removes the email verification code stored in Redis
     * @param email the email
     */
    private void deleteEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        stringRedisTemplate.delete(key);
    }

    /**
     * Gets the email verification code stored in Redis
     * @param email the email
     * @return the verification code
     */
    private String getEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * Rate limits the email verification code requests for a specific IP address
     * @param address the address
     * @return whether the verification passed
     */
    private boolean verifyLimit(String address) {
        String key = Const.VERIFY_EMAIL_LIMIT + address;
        return flow.limitOnceCheck(key, verifyLimit);
    }

    /**
     * Finds an account by username or email
     * @param text the username or email
     * @return the account entity
     */
    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one();
    }

    /**
     * Checks if an account with the specified email already exists
     * @param email the email
     * @return whether the account exists
     */
    private boolean existsAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    /**
     * Checks if an account with the specified username already exists
     * @param username the username
     * @return whether the account exists
     */
    private boolean existsAccountByUsername(String username){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }
}
