package io.hhplus.tdd.point.charge;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.tdd.point.dto.requests.ChargeRequestBody;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ChargePointIntegrationTest {

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
