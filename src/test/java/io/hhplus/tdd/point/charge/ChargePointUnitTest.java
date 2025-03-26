package io.hhplus.tdd.point.charge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.exceptions.CustomInvalidRequestException;
import io.hhplus.tdd.point.UserPointLockManager;
import io.hhplus.tdd.point.domain.ErrorCode;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.requests.ChargeRequest;
import io.hhplus.tdd.point.dto.responses.ChargeResponse;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ChargePointUnitTest {

	@InjectMocks
	private PointServiceImpl pointService;

	@Mock
	private PointHistoryRepository pointHistoryRepository;

	@Mock
	private UserPointRepository userPointRepository;

	@Mock
	private UserPointLockManager userPointLockManager;

	@BeforeEach
	void setUp() {
		pointService = new PointServiceImpl(userPointRepository, pointHistoryRepository, userPointLockManager);
	}


	@Test
	void id가_0이하로_유효하지않으면__CustomInvalidRequestException_예외발생() {
		// given
		long invalidId = 0L; // 1보다 작은 숫자
		long amount = 1000L;

		// when & then
		CustomInvalidRequestException ex  = assertThrows(
			CustomInvalidRequestException.class,
			() -> pointService.charge(new ChargeRequest(invalidId, amount))
		);

		assertEquals(ErrorCode.ID_POSITIVE_NUMBER_POLICY.getMessage(), ex.getMessage());
	}

	@Test
	void 충전금액이_0이하로_유효하지않으면_CustomInvalidRequestException_예외발생() {
		// given
		long id = 1L;
		long invalidAmount =  0L; // 0이하의 부적절한 금액

		// when & then
		CustomInvalidRequestException ex  = assertThrows(
			CustomInvalidRequestException.class,
			() -> pointService.charge(new ChargeRequest(id, invalidAmount))
		);

		assertEquals(ErrorCode.AMOUNT_POSITIVE_NUMBER_POLICY.getMessage(), ex.getMessage());
	}

	@Test
	void 충전금액_amount가__MIN_CHARGE_POINT_AMOUNT_미만으로_유효하지않으면__CustomInvalidRequestException_예외발생() {
		// given
		long id = 1L;
		long invalidAmount = 100L; // 최소금액보다 미달

		// when & then
		CustomInvalidRequestException ex = assertThrows(
			CustomInvalidRequestException.class,
			() -> pointService.charge(new ChargeRequest(id, invalidAmount))
		);

		assertEquals(ErrorCode.MIN_CHARGE_AMOUNT_VALUE_POLICY.getMessage(), ex.getMessage());
	}

	@Test
	void 충전금액_amount가__MAX_CHARGE_POINT_AMOUNT_초과로_유효하지않으면__CustomInvalidRequestException_예외발생() {
		// given
		long id = 1L;
		long invalidAmount = 100_000L; // 최대금액 초과

		// when & then
		CustomInvalidRequestException ex = assertThrows(
			CustomInvalidRequestException.class,
			() -> pointService.charge(new ChargeRequest(id, invalidAmount))
		);

		assertEquals(ErrorCode.MAX_CHARGE_AMOUNT_VALUE_POLICY.getMessage(), ex.getMessage());
	}

	@Test
	void 보유포인트_10000원에_5000원_충전하면_잔액은_15000원이된다() {
		// given
		long id = 1L;
		long initialPoint = 10000L; // 초기 보유 잔액
		long amount = 5000L; // 충전금액

		UserPoint myPoint = new UserPoint(id, initialPoint, 100L);
		long pointAfterCharged = myPoint.charge(amount);

		when(userPointRepository.findById(id)).thenReturn(myPoint); // 충전전 보유잔액
		when(userPointRepository.save(id, pointAfterCharged))
			.thenReturn(new UserPoint(id, pointAfterCharged, myPoint.updateMillis())); // 충전후 보유잔액
		when(userPointLockManager.getLock(id)).thenReturn(new ReentrantLock());

		// when
		ChargeResponse response = pointService.charge(new ChargeRequest(id, amount)); // 포인트 5000원 충전 수행

		// then
		assertEquals(myPoint.charge(amount), response.point()); // 충전후 예상값과 실제값 비교
		verify(pointHistoryRepository, times(1)).insert(id, amount, TransactionType.CHARGE); // 포인트내역 insert 호출검증
		verify(userPointRepository, times(1)).save(id, pointAfterCharged );  // save(포인트정보 수정) 호출검증
	}
}
