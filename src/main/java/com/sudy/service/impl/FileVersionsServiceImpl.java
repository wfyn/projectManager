package com.sudy.service.impl;

import com.sudy.entity.FileVersions;
import com.sudy.mapper.FileVersionsMapper;
import com.sudy.service.IFileVersionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件版本控制表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-07-04
 */
@Service
public class FileVersionsServiceImpl extends ServiceImpl<FileVersionsMapper, FileVersions> implements IFileVersionsService {

}
