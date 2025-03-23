package io.hhplus.tdd.point.service;

import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.exceptions.CustomBusinessException;
import io.hhplus.tdd.point.domain.ErrorCode;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.requests.ChargeRequest;
import io.hhplus.tdd.point.dto.requests.GetHistoriesRequest;
import io.hhplus.tdd.point.dto.requests.GetPointRequest;
import io.hhplus.tdd.point.dto.requests.UseRequest;
import io.hhplus.tdd.point.dto.responses.ChargeResponse;
import io.hhplus.tdd.point.dto.responses.GetPointHistoriesResponse;
import io.hhplus.tdd.point.dto.responses.GetPointResponse;
import io.hhplus.tdd.point.dto.responses.UseResponse;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

	private final UserPointRepository userPointRepository;
	private final PointHistoryRepository pointHistoryRepository;

	@Override
	public GetPointResponse getPoint(GetPointRequest request) {
		UserPoint result = this.userPointRepository.findById(request.id());
		return GetPointResponse.from(result);
	}


	@Override
	public ChargeResponse charge(ChargeRequest request) {
		long id = request.id();
		long amount = request.amount();

		UserPoint userPoint = this.userPointRepository.findById(id);
		long myPoint = userPoint.point();

		// 포인트내역에 '충전' 기록
		this.pointHistoryRepository.insert(id, amount, TransactionType.CHARGE);

		// 포인트 충전
		UserPoint result = this.userPointRepository.save(id, myPoint + amount);
		return ChargeResponse.from(result);
	}


	@Override
	public GetPointHistoriesResponse getPointHistories(GetHistoriesRequest request) {
		long userId = request.id();
		List<PointHistory> result = this.pointHistoryRepository.findAllPointHistoriesByUserPointId(userId);
		return GetPointHistoriesResponse.from(result);
	}


	@Override
	public UseResponse use(UseRequest request) {
		long id = request.id();
		long amount = request.amount();

		UserPoint userPoint = this.userPointRepository.findById(id);
		long myPoint = userPoint.point();

		// 잔고가 사용금액보다 부족할 경우
		if( amount > myPoint ) {
			throw new CustomBusinessException(ErrorCode.OVER_USE_AMOUNT_VALUE_THAN_BALANCE_POLICY);
		}

		// 포인트내역에 '사용' 기록
		this.pointHistoryRepository.insert(id, amount, TransactionType.USE);

		// 포인트 사용
		UserPoint result = this.userPointRepository.save(id, myPoint - amount);
		return UseResponse.from(result);
	}
}
