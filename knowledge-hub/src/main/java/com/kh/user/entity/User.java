package com.kh.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应表 user。
 */
@Data
@TableName("user")
public class User {

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** BCrypt 密码哈希 */
    private String passwordHash;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 角色: USER/ADMIN */
    private String role;

    /** 存储配额(字节), 默认10GB */
    private Long storageQuota;

    /** 已用存储(字节) */
    private Long storageUsed;

    /** 状态: 1正常 0禁用 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
