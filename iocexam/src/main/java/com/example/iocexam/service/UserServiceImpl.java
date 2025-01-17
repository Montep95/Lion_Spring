package com.example.iocexam.service;

import com.example.iocexam.domain.User;
import com.example.iocexam.repository.UserDao;

public class UserServiceImpl implements UserService{
    private UserDao userDao;

    @Override
    public void joinUser(User user) {
        userDao.addUser(user); // 유저를 넣어보내 저장하는 메소드
    }
}
