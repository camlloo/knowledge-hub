package com.kh.tag.controller;

import com.kh.tag.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分类接口（阶段①实现，清单见 docs/02-文件模块接口设计.md）。
 * <p>TODO GET    /v1/categories      分类列表(含文件计数)
 * TODO POST   /v1/categories      创建
 * TODO PUT    /v1/categories/{id} 改名/图标
 * TODO DELETE /v1/categories/{id} 删除(其下文件置空)
 */
@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
}
