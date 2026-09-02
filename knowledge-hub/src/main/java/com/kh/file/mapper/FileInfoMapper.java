package com.kh.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kh.file.entity.FileInfo;

/**
 * 文件表 Mapper，通用 CRUD 由 BaseMapper 提供；
 * 文件名 ngram 全文搜索等自定义 SQL 在实现阶段补充（XML 放 resources/mapper）。
 */
public interface FileInfoMapper extends BaseMapper<FileInfo> {
}
