package com.sudy.service.impl;

import com.sudy.entity.Files;
import com.sudy.mapper.FilesMapper;
import com.sudy.service.IFilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件基本信息表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-07-04
 */
@Service
public class FilesServiceImpl extends ServiceImpl<FilesMapper, Files> implements IFilesService {

}
