package com.lls.user.service;

import com.lls.user.dao.UserDao;
import com.lls.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;

    public User login(String nickname,String phoneNumer) {
        //这里需要校验一下验证码，一般可以把验证码放在redis
        return userDao.findByPhoneNumber(phoneNumer)
                .orElseGet(() -> userDao.save(User.builder()
                        .nickname(nickname)
                        .phoneNumber(phoneNumer).build()));
    }

    public User getUser(long userId) {
        return userDao.findById(userId).get();
    }
}
