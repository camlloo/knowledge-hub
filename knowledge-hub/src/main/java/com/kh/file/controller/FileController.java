package com.kh.file.controller;

import com.kh.file.service.FileInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件接口（阶段①实现，清单见 docs/02-文件模块接口设计.md）。
 * <p>TODO GET    /v1/files/upload/hash-check  秒传预检
 * TODO POST   /v1/files/upload             上传文件（multipart）
 * TODO GET    /v1/files                    分页列表（folderId/tagId/categoryId/keyword）
 * TODO GET    /v1/files/search             文件名全文搜索（ngram + 高亮）
 * TODO GET    /v1/files/{id}               详情
 * TODO PUT    /v1/files/{id}               重命名/移动/改分类
 * TODO DELETE /v1/files/{id}               删除（软删进回收站）
 * TODO PUT    /v1/files/{id}/star          收藏
 * TODO DELETE /v1/files/{id}/star          取消收藏
 * TODO GET    /v1/files/starred            收藏列表
 * TODO GET    /v1/files/recent             最近访问
 * TODO GET    /v1/files/{id}/download-url  下载预签名URL
 * TODO GET    /v1/files/{id}/preview-url   预览预签名URL
 */
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileInfoService fileInfoService;
}
