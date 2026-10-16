package com.x.xaiagent.service.impl;

import com.x.xaiagent.constant.PointsPackageConst;
import com.x.xaiagent.service.PointsPackageService;
import com.x.xaiagent.vo.PointsPackageVO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分充值档位服务：从代码常量 {@link PointsPackageConst} 读取并转 VO。
 */
@Service
public class PointsPackageServiceImpl implements PointsPackageService {

    @Override
    public List<PointsPackageVO> listPackages() {
        return PointsPackageConst.LIST.stream()
                .sorted(Comparator.comparingInt(PointsPackageConst.Def::sort))
                .map(d -> {
                    PointsPackageVO vo = new PointsPackageVO();
                    vo.setKey(d.key());
                    vo.setTitle(d.title());
                    vo.setPrice(d.price());
                    vo.setTotalPoints(d.totalPoints());
                    vo.setBonusPoints(d.bonusPoints());
                    vo.setTag(d.tag());
                    vo.setDesc(d.desc());
                    vo.setHot(d.hot());
                    vo.setSort(d.sort());
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
