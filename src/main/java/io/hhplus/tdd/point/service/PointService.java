package io.hhplus.tdd.point.service;

import java.util.List;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.requests.ChargeRequest;
import io.hhplus.tdd.point.dto.requests.GetHistoriesRequest;
import io.hhplus.tdd.point.dto.requests.GetPointRequest;
import io.hhplus.tdd.point.dto.requests.UseRequest;
import io.hhplus.tdd.point.dto.responses.ChargeResponse;
import io.hhplus.tdd.point.dto.responses.GetPointHistoriesResponse;
import io.hhplus.tdd.point.dto.responses.GetPointResponse;
import io.hhplus.tdd.point.dto.responses.UseResponse;

public interface PointService {

    /**
     * 특정 유저의 포인트를 조회 기능
     */
    GetPointResponse getPoint(GetPointRequest request);

    /**
     * charge: 특정 유저의 포인트를 충전하는 기능
     */
    ChargeResponse charge(ChargeRequest request);

    /**
     * 특정 유저의 포인트를 사용하는 기능
     */
    UseResponse use(UseRequest request);

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
     */
    GetPointHistoriesResponse getPointHistories (GetHistoriesRequest request);
}
