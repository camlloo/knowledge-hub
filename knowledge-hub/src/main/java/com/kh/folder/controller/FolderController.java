package com.kh.folder.controller;

import com.kh.folder.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件夹接口（阶段①实现，清单见 docs/02-文件模块接口设计.md）。
 * <p>TODO GET    /v1/folders/tree   整棵目录树
 * TODO POST   /v1/folders        创建子文件夹
 * TODO PUT    /v1/folders/{id}   重命名/移动（禁止移入子孙目录）
 * TODO DELETE /v1/folders/{id}   删除（递归软删，内容进回收站）
 */
@RestController
@RequestMapping("/v1/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
}
