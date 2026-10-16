package com.x.xaiagent.service;

import com.x.xaiagent.vo.PointsPackageVO;

import java.util.List;

/**
 * 积分充值档位（代码常量，用户端查询列表）。
 */
public interface PointsPackageService {

    /** 返回所有上架档位（按 sort 升序） */
    List<PointsPackageVO> listPackages();
}
