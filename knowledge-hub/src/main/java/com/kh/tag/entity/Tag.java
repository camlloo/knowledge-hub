package com.kh.tag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体，对应表 tag。
 */
@Data
@TableName("tag")
public class Tag {

    /** 标签ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 */
    private Long userId;

    /** 标签名 */
    private String name;

    /** 显示颜色 */
    private String color;

    /** 来源: MANUAL人工/AI自动 */
    private String source;

    private LocalDateTime createdAt;
}
