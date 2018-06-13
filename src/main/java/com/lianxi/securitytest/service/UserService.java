package com.lianxi.securitytest.service;

import com.lianxi.securitytest.entity.SysUser;

public interface UserService {

    /**
     * 根据用户名获取系统用户
     */
    SysUser getUserByName(String username);

}
