package com.kh.tag.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文件-标签关联实体，对应表 file_tag（联合主键，无自增id）。
 */
@Data
@TableName("file_tag")
public class FileTag {

    /** 文件ID */
    private Long fileId;

    /** 标签ID */
    private Long tagId;
}
