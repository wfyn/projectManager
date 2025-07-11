package com.sudy.mapper;

import com.sudy.entity.GitCommitHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Git提交历史表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-07-10
 */
@Mapper
public interface GitCommitHistoryMapper extends BaseMapper<GitCommitHistory> {

}
