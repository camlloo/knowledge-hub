package com.kh.user.service.impl;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kh.user.entity.User;
import com.kh.user.mapper.UserMapper;
import com.kh.user.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现（业务方法阶段①实现）。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
