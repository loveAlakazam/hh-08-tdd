package io.hhplus.tdd.point.charge;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


	private static final Logger log = LoggerFactory.getLogger(ChargeConcurrencyTest.class);


	@BeforeEach
	void setUp() {
		// UserPoint 의 초기 포인트값을 0 으로한다.
		userPointRepository.save(1L, 0L);
	}


	@Test
	void 한명의_유저가_동시에_1000원_충전을_100번_요청했을때_정상적으로_합산에_성공해야한다() throws Exception {
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
					// 포인트충전 API 호출
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
		assertEquals(expectedPoint, finalPoint, "충전후 포인트값이 예상값("+expectedPoint+")과 실제값("+finalPoint+")이 서로다릅니다.");
	}

	@Test
	void 서로_다른유저_10명이_동시에_1000원_충전을_요청했을때_정상적으로_합산에_성공한다() throws Exception {
		// given
		int numberOfUsers = 10; // 인원수
		int requestPerUser = 10; // 한사람당 요청개수

		// 포인트 초기화
		for(long id = 1L; id < numberOfUsers ; id++) {
			userPointRepository.save(id, 0L);
		}

		ExecutorService executor = Executors.newFixedThreadPool(5); // 스레드풀: 5개. (요청 5개 동시에 실행가능)
		CountDownLatch latch = new CountDownLatch(numberOfUsers * requestPerUser); // 10 * 10
		long chargeAmount = 1000L;

		// when
		for(long id = 1L; id <= numberOfUsers; id++) {
			ChargeRequestBody requestBody = new ChargeRequestBody(chargeAmount);
			String json = objectMapper.writeValueAsString(requestBody);
			// if(id ==1L) {
			// 	wait(30*1000); // 30초 대기
			// }


			// 유저 1명당 10번을 요청한다.
			for(int i = 0 ; i < requestPerUser; i++) {
				long uid = id;
				executor.submit(() -> {
					try {
						mockMvc.perform(patch("/point/"+ uid +"/charge")
							.contentType(MediaType.APPLICATION_JSON)
							.content(json));
					} catch(Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					} finally {
						latch.countDown();
					}
				});
			}
		}

		latch.await();
		Thread.sleep(1000);

		// then
		log.info("::: 테스트 종료후 유저별 보유포인트 조회 :::");
		for(long id = 1L; id <= numberOfUsers; id++){
			long actualPoint = userPointRepository.findById(id).point();
			log.info("유저 ID {} 의 보유포인트: {}", id, actualPoint);
		}

		for(long id =1L; id <= numberOfUsers; id++) {
			long actualPoint = userPointRepository.findById(id).point();
			assertEquals(requestPerUser * chargeAmount, actualPoint,"UserPoint id "+id+ "인 회원의 포인트는 예상금액과 다릅니다: "+ actualPoint + "원" );
		}
	}
}
