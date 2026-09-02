package com.kh.file.controller;

import com.kh.file.service.RecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站接口（阶段①实现，清单见 docs/02-文件模块接口设计.md）。
 * <p>TODO GET    /v1/recycle                 回收站分页列表
 * TODO PUT    /v1/recycle/{fileId}/restore 恢复
 * TODO DELETE /v1/recycle/{fileId}         彻底删除（联动 MinIO）
 * TODO DELETE /v1/recycle                  清空回收站
 */
@RestController
@RequestMapping("/v1/recycle")
@RequiredArgsConstructor
public class RecycleController {

    private final RecycleService recycleService;
}
