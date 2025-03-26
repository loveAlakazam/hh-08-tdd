package io.hhplus.tdd.point;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;


@Component
public class UserPointLockManager {
	// 사용자별 분리된 락을 관리하는 맵
	// 1. 사용자 ID(id) 마다 하나의 고유한 락(Object)를 저장하는 맵
	// 2. synchronized에 넘길 락을 하나만 쓰면 전역락(exclusive lock)이 되므로 사용자별로 분리된 락객체를 관리
	private final ConcurrentHashMap<Long, Object> locks = new ConcurrentHashMap<>();

	// getLock: 사용자 ID(id)에 해당하는 락(lock)을 획득.
	public Object getLock(Long id) {
		// locks.computeIfAbsent(id, key -> new Object());
		// 1. 사용자 ID(id)에 해당하는 락객체가 이미 있으면 그 객체를 반환하고, 없다면 새로운 락을 넣는다.
		// 2. 사용자 ID(id)별 하나의 고유한 락을 필요할 때만 만들고 중복으로 만들지않도록 보장한다.
		return locks.computeIfAbsent(id, key -> new Object());
	}
}
