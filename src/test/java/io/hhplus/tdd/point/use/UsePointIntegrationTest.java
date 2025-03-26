package io.hhplus.tdd.point.use;

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

import io.hhplus.tdd.point.dto.requests.UseRequestBody;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UsePointIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserPointRepository userPointRepository;

	@Autowired
	private PointHistoryRepository pointHistoryRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void 포인트사용_성공() throws Exception {
		// given
		long id = 1L;
		long initialPoint = 5000L;
		long amount = 1000L;

		userPointRepository.save(id, initialPoint); // 사용이전 잔액 5000원

		UseRequestBody requestBody = new UseRequestBody(amount);
		String json = objectMapper.writeValueAsString(requestBody);

		// when & then
		long expectedPoint = initialPoint - amount;
		mockMvc.perform(
				patch("/point/"+id+"/use")
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.point").value(expectedPoint));
		;
	}
}
