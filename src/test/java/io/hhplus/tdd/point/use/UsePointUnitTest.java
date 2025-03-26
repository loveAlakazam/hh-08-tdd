package io.hhplus.tdd.point.use;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.exceptions.CustomBusinessException;
import io.hhplus.tdd.exceptions.CustomInvalidRequestException;
import io.hhplus.tdd.point.domain.ErrorCode;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.requests.ChargeRequest;
import io.hhplus.tdd.point.dto.requests.UseRequest;
import io.hhplus.tdd.point.dto.responses.UseResponse;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UsePointUnitTest {

	@InjectMocks
	private PointServiceImpl pointService;

	@Mock
	private PointHistoryRepository pointHistoryRepository;

	@Mock
	private UserPointRepository userPointRepository;

	@BeforeEach
	void setUp() {
		pointService = new PointServiceImpl(userPointRepository, pointHistoryRepository);
	}


	@Test
	void id가_0이하로_유효하지않으면__CustomInvalidRequestException_예외발생() {
		// given
		long invalidId = 0L; // 1보다 작은 숫자
		long amount = 1000L;

		// when & then
		CustomInvalidRequestException ex  = assertThrows(
			CustomInvalidRequestException.class,
			() -> pointService.use(new UseRequest(invalidId, amount))
		);

		assertEquals(ErrorCode.ID_POSITIVE_NUMBER_POLICY.getMessage(), ex.getMessage());
	}

	@Test
	void 사용금액이_0이하로_유효하지않으면_CustomInvalidRequestException_예외발생() {
		// given
		long id = 1L;
		long invalidAmount =  0L; // 0이하의 부적절한 금액

		// when & then
		CustomInvalidRequestException ex  = assertThrows(
			CustomInvalidRequestException.class,
			() -> pointService.use(new UseRequest(id, invalidAmount))
		);

		assertEquals(ErrorCode.AMOUNT_POSITIVE_NUMBER_POLICY.getMessage(), ex.getMessage());
	}

	@Test
	void 사용금액_amount가__MIN_USE_AMOUNT_VALUE_POLICY_미만으로_유효하지않으면__CustomInvalidRequestException_예외발생() {
		// given
		long id = 1L;
		long invalidAmount = 99L; // 최소금액보다 미달

		// when & then
		CustomInvalidRequestException ex = assertThrows(
			CustomInvalidRequestException.class,
			() -> pointService.use(new UseRequest(id, invalidAmount))
		);

		assertEquals(ErrorCode.MIN_USE_AMOUNT_VALUE_POLICY.getMessage(), ex.getMessage());
	}

	@Test
	void 사용금액_amount가__MAX_USE_AMOUNT_VALUE_POLICY_초과로_유효하지않으면__CustomInvalidRequestException_예외발생() {
		// given
		long id = 1L;
		long invalidAmount = 100_000L; // 최대금액 초과

		// when & then
		CustomInvalidRequestException ex = assertThrows(
			CustomInvalidRequestException.class,
			() -> pointService.use(new UseRequest(id, invalidAmount))
		);

		assertEquals(ErrorCode.MAX_USE_AMOUNT_VALUE_POLICY.getMessage(), ex.getMessage());
	}

	@Test
	void 사용금액_amount가_잔액보다_클_경우__CustomBusinessException_예외발생() {
		// given
		long id = 1L;
		long amount = 5000L;

		// 보유잔액이 amount 보다 작음
		UserPoint mockUserPoint = new UserPoint(id, 4500L, 100L);
		when(userPointRepository.findById(id)).thenReturn(mockUserPoint);

		// when & then
		CustomBusinessException ex = assertThrows(
			CustomBusinessException.class,
			() -> pointService.use(new UseRequest(id, amount))
		);

		assertEquals(ErrorCode.OVER_USE_AMOUNT_VALUE_THAN_BALANCE_POLICY.getMessage(), ex.getMessage());;

		// 포인트조회만 1번 호출
		verify(userPointRepository, times(1)).findById(id);
		// 포인트 사용 내역 호출 없음
		verify(pointHistoryRepository, never()).insert(id, amount, TransactionType.USE);
		// 변경 호출 없음
		verify(userPointRepository, never()).save(id, amount);
	}

	@Test
	void 포인트사용_성공() {
		// given
		long id = 1L;
		long amount = 5000L;

		UserPoint mockUserPoint = new UserPoint(id, 15000L, 100L); // 사용 이전 보유잔액 15000L
		when(userPointRepository.findById(id)).thenReturn(mockUserPoint);

		long myPoint = mockUserPoint.point();
		long expectedPoint = myPoint - amount; // 사용 이후 예상 보유잔액: 10000L

		UserPoint mockResult = new UserPoint(id, myPoint - amount, 100L);
		when(userPointRepository.save(id, myPoint - amount)).thenReturn(mockResult);

		// when
		UseResponse response = this.pointService.use(new UseRequest(id, amount));

		// then
		verify(userPointRepository, times(1)).findById(id); // 포인트조회만 1번 호출
		verify(pointHistoryRepository, times(1)).insert(id, amount, TransactionType.USE); // 포인트 사용 내역 1번 호출
		verify(userPointRepository, times(1)).save(id, myPoint - amount); // 변경 1번 호출

		assertEquals(expectedPoint, response.point());
	}
}
