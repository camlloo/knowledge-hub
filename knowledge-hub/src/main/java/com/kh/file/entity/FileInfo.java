package com.kh.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件元数据实体，对应表 file（文件本体存 MinIO；deleted 走全局逻辑删除配置）。
 */
@Data
@TableName("file")
public class FileInfo {

    /** 文件ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 */
    private Long userId;

    /** 所在文件夹 */
    private Long folderId;

    /** 文件名(含扩展名) */
    private String name;

    /** 扩展名, 小写 */
    private String ext;

    /** MIME类型 */
    private String mimeType;

    /** 大小(字节) */
    private Long size;

    /** MinIO 对象键 */
    private String storageKey;

    /** 文件哈希(秒传/去重) */
    private String sha256;

    /** 分类 */
    private Long categoryId;

    /** AI 摘要(阶段②) */
    private String summary;

    /** 处理状态: UPLOADED/PARSING/SUMMARIZING/CHUNKED/EMBEDDED/GRAPH_DONE/READY/FAILED */
    private String status;

    /** 状态详情(失败原因等) */
    private String statusDetail;

    /** 是否收藏 */
    private Boolean star;

    /** 切片数(阶段②) */
    private Integer chunkCount;

    /** 关联笔记(阶段⑤) */
    private Long noteId;

    /** 最近访问时间 */
    private LocalDateTime lastAccessAt;

    /** 逻辑删除(回收站) */
    private Integer deleted;

    /** 删除时间(回收站30天保留) */
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
