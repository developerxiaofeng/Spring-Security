package com.lianxi.securitytest.service.impl;

import com.lianxi.securitytest.dao.UserDao;
import com.lianxi.securitytest.entity.SysUser;
import com.lianxi.securitytest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Cacheable(cacheNames = "authority", key = "#username")
    @Override
    public SysUser getUserByName(String username) {
        return userDao.selectByName(username);
    }
}
