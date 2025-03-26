package io.hhplus.tdd.point.charge;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.UserPointLockManager;
import io.hhplus.tdd.point.dto.requests.ChargeRequestBody;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.PointHistoryRepositoryImpl;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 인스턴스를 공유
public class ChargeConcurrencyTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PointService pointService;

	@Autowired
	private UserPointRepository userPointRepository;

	@Autowired
	private PointHistoryRepository pointHistoryRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserPointLockManager userPointLockManager;


	@BeforeEach
	void setUp() {
		// UserPoint 의 초기 포인트값을 0 으로한다.
		userPointRepository.save(1L, 0L);
	}

	@AfterEach
	void tearDown() throws NoSuchFieldException, IllegalAccessException {
		// PointHistoryTable 코드를 변경금지 룰을 지켜야하기때문에
		// private 요소인 PointHistoryTable을 외부에 가져와야하는 Reflection을 사용할수 밖에 없었습니다.
		// PointHistoryTable 인스턴스를 리플렉션으로 가져옴
		Field field = PointHistoryRepositoryImpl.class.getDeclaredField("pointHistoryTable");
		field.setAccessible(true);
		PointHistoryTable pointHistoryTable = (PointHistoryTable) field.get(pointHistoryRepository);

		// table 필드 접근해서 clear()
		Field tableField = PointHistoryTable.class.getDeclaredField("table");
		tableField.setAccessible(true);
		List<?> table = (List<?>) tableField.get(pointHistoryTable);
		table.clear();

		// cursor도 초기화 (선택적)
		Field cursorField = PointHistoryTable.class.getDeclaredField("cursor");
		cursorField.setAccessible(true);
		cursorField.set(pointHistoryTable, 1L);
	}

	@Test
	void 동시에_100번_충전요청을_요청했을때_정상적으로_합산에_성공해야한다() throws Exception {
		// given
		long id = 1L;
		int threadCount = 100;
		long chargeAmount = 1000L;

		ExecutorService executor = Executors.newFixedThreadPool(10); // 스레드풀 10개
		CountDownLatch latch = new CountDownLatch(threadCount); // 요청가능한 스레드개수
		ChargeRequestBody requestBody = new ChargeRequestBody(chargeAmount);
		String json = objectMapper.writeValueAsString(requestBody);

		// when
		for(int i = 0 ; i < threadCount ; i++) {
			executor.submit(()-> {
				try {
					mockMvc.perform(patch("/point/"+id+"/charge")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json));
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} finally {
					latch.countDown(); // 요청가능 스레드 개수 감소
				}
			});
		}

		latch.await(); // 다 끝날 때까지 대기
		Thread.sleep(1000); // 1초 정도 대기후 최종 포인트 확인

		// then
		long expectedPoint = chargeAmount * threadCount;
		long finalPoint = userPointRepository.findById(id).point();
		assertEquals(expectedPoint, finalPoint);
	}
}
