package com.sudy.mapper;

import com.sudy.entity.GitRepository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * Git仓库信息表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-07-10
 */
@Mapper
public interface GitRepositoryMapper extends BaseMapper<GitRepository> {

    @Select("SELECT * FROM git_repository WHERE repo_path = #{repoPath}")
    GitRepository selectByRepoPath(String repoPath);
}
