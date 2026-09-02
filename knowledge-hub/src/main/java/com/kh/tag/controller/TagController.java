package com.kh.tag.controller;

import com.kh.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标签接口（阶段①实现，清单见 docs/02-文件模块接口设计.md）。
 * <p>TODO GET    /v1/tags                    标签列表(含使用计数)
 * TODO POST   /v1/tags                    创建
 * TODO PUT    /v1/tags/{id}               重命名/改色
 * TODO DELETE /v1/tags/{id}               删除并解除关联
 * TODO POST   /v1/tags/merge              合并 {sourceId, targetId}
 * TODO POST   /v1/files/{id}/tags         给文件打标签
 * TODO DELETE /v1/files/{id}/tags/{tagId} 移除文件标签
 */
@RestController
@RequestMapping("/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
}
