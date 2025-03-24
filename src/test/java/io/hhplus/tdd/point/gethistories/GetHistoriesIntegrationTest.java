package io.hhplus.tdd.point.gethistories;

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

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.domain.ErrorCode;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.PointHistoryRepositoryImpl;
import io.hhplus.tdd.point.repository.UserPointRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class GetHistoriesIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserPointRepository userPointRepository;

	@Autowired
	private PointHistoryRepository pointHistoryRepository;

	@AfterEach
	void tearDown() throws NoSuchFieldException, IllegalAccessException {
		// 여러개의 통합테스트를 한꺼번에 테스트할 경우에 이전의 히스토리테이블에 데이터가 누적되는 경우가 있습니다.
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
	void 포인트내역조회_성공() throws Exception {
		// given
		long id = 1L;

		pointHistoryRepository.insert(id,1500 , TransactionType.CHARGE); // 1500원 충전
		pointHistoryRepository.insert(id,1000 , TransactionType.USE); // 1000원 사용

		// when & then
		mockMvc.perform(
				get("/point/"+id+"/histories")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.histories[0].amount").value(1500L))  // 1번째 내역 amount 값 체크
			.andExpect(jsonPath("$.histories[0].type").value(TransactionType.CHARGE.name()))  // 1번째 내역 type 값 체크
			.andExpect(jsonPath("$.histories[1].amount").value(1000L))  // 2번째 내역 amount 값 체크
			.andExpect(jsonPath("$.histories[1].type").value(TransactionType.USE.name()))  // 2번째 내역 type 값 체크
		;
	}
}
