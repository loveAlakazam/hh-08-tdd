package io.hhplus.tdd.point.getpoint;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.exceptions.CustomInvalidRequestException;
import io.hhplus.tdd.point.domain.ErrorCode;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.requests.GetPointRequest;
import io.hhplus.tdd.point.dto.responses.GetPointResponse;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointServiceImpl;

@ExtendWith(MockitoExtension.class)
public class GetPointUnitTest {

	@InjectMocks
	private PointServiceImpl pointService;

	@Mock
	private UserPointRepository userPointRepository;

	@Mock
	private PointHistoryRepository pointHistoryRepository;

	@BeforeEach
	void setUp() {
		pointService = new PointServiceImpl(userPointRepository, pointHistoryRepository);
	}

	@Test
	void id가_0이하로_유효하지않으면__CustomInvalidRequestException_예외발생() {
		// given
		long invalidId = 0L; // 1보다 작은 숫자

		// when & then
		CustomInvalidRequestException ex  = assertThrows(
			CustomInvalidRequestException.class,
			() -> pointService.getPoint(new GetPointRequest(invalidId))
		);

		assertEquals(ErrorCode.ID_POSITIVE_NUMBER_POLICY.getMessage(), ex.getMessage());
	}

	@Test
	void 포인트조회성공() {
		// given
		long id = 1L;

		// when
		UserPoint mockResult = new UserPoint(id, 1000L, 100L);
		when(userPointRepository.findById(id)).thenReturn(mockResult);

		GetPointResponse response = pointService.getPoint(new GetPointRequest(id));

		// then
		verify(userPointRepository, times(1)).findById(id);
		assertEquals(1000L, response.point());
	}

}
