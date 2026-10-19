package com.x.xaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.x.xaiagent.entity.User;
import com.x.xaiagent.vo.DayCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 统计全部账号数（含已删除，绕过 @TableLogic 逻辑删除过滤，SQL 见 UserMapper.xml）。
     */
    Long countAll();

    /**
     * 按天分组统计新增用户数（不含已删除，SQL 见 UserMapper.xml）。
     */
    List<DayCountVO> countNewByDay(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计某时间点之前的累计注册数（不含已删除，SQL 见 UserMapper.xml）。
     */
    Long countCreatedBefore(@Param("startTime") LocalDateTime startTime);

    /**
     * 用户分页列表（不含已删除）：用户名/手机号模糊、角色/状态精确，按创建时间倒序。
     * 分页由 MyBatis-Plus 的 Page 参数驱动，SQL 见 UserMapper.xml。
     */
    Page<User> selectUserPage(Page<User> page,
                              @Param("username") String username,
                              @Param("phone") String phone,
                              @Param("role") String role,
                              @Param("status") String status);
}
