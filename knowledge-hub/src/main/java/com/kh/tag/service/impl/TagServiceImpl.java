package com.kh.tag.service.impl;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kh.tag.entity.Tag;
import com.kh.tag.mapper.TagMapper;
import com.kh.tag.service.TagService;
import org.springframework.stereotype.Service;

/**
 * 标签服务实现（业务方法阶段①实现）。
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}
