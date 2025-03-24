package io.hhplus.tdd.point.charge;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.dto.requests.ChargeRequestBody;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.PointHistoryRepositoryImpl;
import io.hhplus.tdd.point.repository.UserPointRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class ChargePointIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserPointRepository userPointRepository;

	@Autowired
	private PointHistoryRepository pointHistoryRepository;

	@Autowired
	private ObjectMapper objectMapper;

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
	void 충전_이전금액이_0원일때_포인트충전_성공() throws Exception {
		// given
		long id = 1L;
		long initialPoint = 0L;
		long amount = 10000L; // 충전금액

		userPointRepository.save(id, initialPoint); // 충전이전 잔액 0원

		ChargeRequestBody requestBody = new ChargeRequestBody(amount);
		String json = objectMapper.writeValueAsString(requestBody);

		// when & then
		long expectedPoint = initialPoint + amount; // 충전이후 예상 잔액 10000원
		mockMvc.perform(
			patch("/point/"+id+"/charge")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.point").value(expectedPoint));
	}

	@Test
	void 충전_이전금액이_0원이아닐때_포인트충전_성공() throws Exception {
		// given
		long id = 1L;
		long initialPoint = 5000L;
		long amount = 10000L; // 충전금액

		userPointRepository.save(id, initialPoint); // 충전 이전 잔액 5000원

		ChargeRequestBody requestBody = new ChargeRequestBody(amount);
		String json = objectMapper.writeValueAsString(requestBody);

		// when & then
		long expectedPoint = initialPoint + amount; // 충전 이후 예상잔액 15000원
		mockMvc.perform(
				patch("/point/"+id+"/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.point").value(expectedPoint));
	}

}
