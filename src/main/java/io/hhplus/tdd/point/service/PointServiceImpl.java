package io.hhplus.tdd.point.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.hhplus.tdd.exceptions.CustomBusinessException;
import io.hhplus.tdd.point.UserPointLockManager;
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
	private static final Logger log = LoggerFactory.getLogger(PointServiceImpl.class);

	@Override
	public GetPointResponse getPoint(GetPointRequest request) {
		UserPoint result = this.userPointRepository.findById(request.id());
		return GetPointResponse.from(result);
	}


	@Override
	public synchronized  ChargeResponse charge(ChargeRequest request) {
		// 사용자별 lock을 사용하여 다른접근 요청을 제한한다. (임계구역)
		long id = request.id();
		long amount = request.amount();

		// 로그기록
		log.info("::: 🔒 Lock acquired for userId: {}, thread: {}", id, Thread.currentThread().getName());

		// 보유 포인트 조회
		UserPoint myPoint = this.userPointRepository.findById(id);

		// 포인트 충전
		long pointAfterCharge = myPoint.charge(amount);

		// 포인트내역에 '충전' 기록
		this.pointHistoryRepository.insert(id, amount, TransactionType.CHARGE);

		// 보유포인트 정보 수정
		UserPoint result = this.userPointRepository.save(id, pointAfterCharge);
		log.info("::: {} 작업완료: 유저 id {}의 충전후 보유 포인트: {}", Thread.currentThread().getName(), id, result.point() );
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

		// 보유 포인트 조회
		UserPoint myPoint = this.userPointRepository.findById(id);

		// 포인트 사용
		long pointAfterUse = myPoint.use(amount);

		// 포인트내역에 '사용' 기록
		this.pointHistoryRepository.insert(id, amount, TransactionType.USE);

		// 보유포인트 정보 수정
		UserPoint result = this.userPointRepository.save(id, pointAfterUse);
		return UseResponse.from(result);
	}
}
