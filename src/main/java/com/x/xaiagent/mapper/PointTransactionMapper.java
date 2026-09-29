package com.x.xaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.x.xaiagent.entity.PointTransaction;
import com.x.xaiagent.vo.AdminPointUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PointTransactionMapper extends BaseMapper<PointTransaction> {

    /**
     * 累计获得积分（正数流水之和，SQL 见 PointTransactionMapper.xml）。
     */
    Long sumEarnedPoints(@Param("userId") String userId);

    /**
     * 累计消费积分（负数流水取绝对值之和，SQL 见 PointTransactionMapper.xml）。
     */
    Long sumSpentPoints(@Param("userId") String userId);

    /**
     * 管理端用户积分列表：按用户聚合余额/累计获得/累计消费/最近变动时间（SQL 见 PointTransactionMapper.xml）。
     * 配合 MyBatis-Plus 分页插件使用，keyword 为用户名模糊匹配（可空）。
     */
    IPage<AdminPointUserVO> selectUserPointPage(Page<AdminPointUserVO> page, @Param("keyword") String keyword);
}
