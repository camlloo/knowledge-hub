package com.kh.tag.service.impl;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kh.tag.entity.Category;
import com.kh.tag.mapper.CategoryMapper;
import com.kh.tag.service.CategoryService;
import org.springframework.stereotype.Service;

/**
 * 分类服务实现（业务方法阶段①实现）。
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
