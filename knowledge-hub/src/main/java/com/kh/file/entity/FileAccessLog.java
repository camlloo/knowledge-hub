package com.kh.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件访问记录实体，对应表 file_access_log（"最近访问"数据来源）。
 */
@Data
@TableName("file_access_log")
public class FileAccessLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 */
    private Long userId;

    /** 文件ID */
    private Long fileId;

    /** 行为: view/download/preview */
    private String action;

    private LocalDateTime createdAt;
}
