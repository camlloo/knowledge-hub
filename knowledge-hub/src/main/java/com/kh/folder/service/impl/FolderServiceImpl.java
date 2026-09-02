package com.kh.folder.service.impl;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kh.folder.entity.Folder;
import com.kh.folder.mapper.FolderMapper;
import com.kh.folder.service.FolderService;
import org.springframework.stereotype.Service;

/**
 * 文件夹服务实现（业务方法阶段①实现）。
 */
@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements FolderService {
}
