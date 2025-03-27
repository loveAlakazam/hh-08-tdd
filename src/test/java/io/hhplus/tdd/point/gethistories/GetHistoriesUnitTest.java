package io.hhplus.tdd.point.gethistories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.exceptions.CustomInvalidRequestException;
import io.hhplus.tdd.point.UserPointLockManager;
import io.hhplus.tdd.point.domain.ErrorCode;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.dto.requests.GetHistoriesRequest;
import io.hhplus.tdd.point.dto.responses.GetPointHistoriesResponse;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointServiceImpl;

@ExtendWith(MockitoExtension.class)
public class GetHistoriesUnitTest {

	@InjectMocks
	private PointServiceImpl pointService;

	@Mock
	private UserPointRepository userPointRepository;

	@Mock
	private PointHistoryRepository pointHistoryRepository;

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

		// when & then
		CustomInvalidRequestException ex  = assertThrows(
			CustomInvalidRequestException.class,
			() -> pointService.getPointHistories(new GetHistoriesRequest(invalidId))
		);

		assertEquals(ErrorCode.ID_POSITIVE_NUMBER_POLICY.getMessage(), ex.getMessage());
	}

	@Test
	void 포인트내역_조회성공 () {
		// given
		long id = 1L;

		// 히스토리기록 mock 데이터
		List<PointHistory> pointHistories = new ArrayList();
		pointHistories.add(new PointHistory(1L, id, 5000L, TransactionType.CHARGE, 100L)); // 5000원 충전
		pointHistories.add(new PointHistory(2L, id, 3500L, TransactionType.USE, 100L)); // 3500원 사용
		when(pointHistoryRepository.findAllPointHistoriesByUserPointId(id)).thenReturn(pointHistories);

		// when
		GetPointHistoriesResponse response = pointService.getPointHistories(new GetHistoriesRequest(id));

		// then
		verify(pointHistoryRepository, times(1)).findAllPointHistoriesByUserPointId(id);

		// 1번째 요소 데이터 검증
		assertEquals(1L, pointHistories.get(0).id()); // id 검증
		assertEquals(5000L, pointHistories.get(0).amount()); // amount 검증
		assertEquals(TransactionType.CHARGE, pointHistories.get(0).type()); // type 검증

		// 2번째 요소 데이터 검증
		assertEquals(2L, pointHistories.get(1).id()); // id 검증
		assertEquals(3500L, pointHistories.get(1).amount()); // amount 검증
		assertEquals(TransactionType.USE, pointHistories.get(1).type()); // type 검증

	}

}
