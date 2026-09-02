package com.kh.tag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类实体，对应表 category。
 */
@Data
@TableName("category")
public class Category {

    /** 分类ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户(0表示系统预置) */
    private Long userId;

    /** 分类名, 如 Java/数据库/AI */
    private String name;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    private LocalDateTime createdAt;
}
