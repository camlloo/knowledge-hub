package com.kh.folder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件夹实体，对应表 folder（deleted 走全局逻辑删除配置）。
 */
@Data
@TableName("folder")
public class Folder {

    /** 文件夹ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 */
    private Long userId;

    /** 父文件夹ID, 根为null */
    private Long parentId;

    /** 文件夹名 */
    private String name;

    /** 物化路径, 如 /docs/java/ */
    private String path;

    /** 排序 */
    private Integer sort;

    /** 逻辑删除 */
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
