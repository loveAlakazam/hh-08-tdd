package io.hhplus.tdd.point.charge;

import static java.lang.Thread.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.requests.ChargeRequest;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.PointHistoryRepositoryImpl;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.repository.UserPointRepositoryImpl;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.service.PointServiceImpl;


public class ChargeConcurrencyServiceTest {

	private PointService pointService;

	private UserPointRepository userPointRepository;

	private PointHistoryRepository pointHistoryRepository;


	private static final Logger log = LoggerFactory.getLogger(ChargeConcurrencyServiceTest.class);

	@BeforeEach
	void setUp() {
		// 직접 의존성 주입(SpringBootTest / Autowired 을 사용시 자동으로 의존성주입)
		pointHistoryRepository = new PointHistoryRepositoryImpl(new PointHistoryTable());
		userPointRepository = new UserPointRepositoryImpl(new UserPointTable());
		pointService = new PointServiceImpl(userPointRepository, pointHistoryRepository);

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
		ChargeRequest request = new ChargeRequest(id , chargeAmount);

		// when
		for(int i = 0 ; i < threadCount ; i++) {
			executor.submit(()-> {
				try {
					// 포인트충전 API 호출
					pointService.charge(request);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} finally {
					latch.countDown(); // 요청가능 스레드 개수 감소
				}
			});
		}

		latch.await(); // 다 끝날 때까지 대기
		sleep(1000); // 1초 정도 대기후 최종 포인트 확인

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
			ChargeRequest request = new ChargeRequest(id, chargeAmount);

			// 유저 1명당 10번을 요청한다.
			for(int i = 0 ; i < requestPerUser; i++) {
				long uid = id;
				executor.submit(() -> {
					try {
						pointService.charge(request);
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
		sleep(1000);

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
