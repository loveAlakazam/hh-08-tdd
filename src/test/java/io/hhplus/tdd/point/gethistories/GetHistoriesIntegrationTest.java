package io.hhplus.tdd.point.gethistories;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
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
