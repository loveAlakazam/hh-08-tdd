package io.hhplus.tdd.point;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class UserPointQueueProcessor {
	private final Map<Long, BlockingQueue<Runnable>> userPointQueue = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {}
}
