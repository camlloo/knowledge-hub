package com.kh.tag.service;


import com.baomidou.mybatisplus.spring.service.IService;
import com.kh.tag.entity.Category;

/**
 * 分类服务：分类 CRUD（删除时其下文件 categoryId 置空）（阶段①实现）。
 */
public interface CategoryService extends IService<Category> {
}
