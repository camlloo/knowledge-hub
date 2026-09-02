package com.kh.file.service.impl;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kh.file.entity.FileInfo;
import com.kh.file.mapper.FileInfoMapper;
import com.kh.file.service.FileInfoService;
import org.springframework.stereotype.Service;

/**
 * 文件服务实现（业务方法阶段①实现）。
 */
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {
}
