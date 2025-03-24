package io.hhplus.tdd.point.getpoint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import io.hhplus.tdd.point.domain.ErrorCode;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class GetPointIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserPointRepository userPointRepository;

	@Test
	void 조회성공() throws Exception {
		// given
		long id = 1L;
		UserPoint userPoint = userPointRepository.save(id, 5000L);

		// when & then
		mockMvc.perform(
				get("/point/"+id)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.point").value(5000L))
		;
	}
}
