package com.x.xaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.xaiagent.entity.NoticeRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NoticeReadMapper extends BaseMapper<NoticeRead> {

    /**
     * 幂等插入已读记录：(user_id, notice_id) 已存在时忽略（PG ON CONFLICT DO NOTHING，SQL 见 NoticeReadMapper.xml）。
     *
     * @return 影响行数（0=已存在，1=新插入）
     */
    int insertIgnore(@Param("id") String id,
                     @Param("userId") String userId,
                     @Param("noticeId") String noticeId);
}
