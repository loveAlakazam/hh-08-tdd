package io.hhplus.tdd.point.repository;

import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserPointRepositoryImpl implements UserPointRepository{
	private final UserPointTable userPointTable;
	@Override
	public UserPoint save(long id, long amount) {
		return this.userPointTable.insertOrUpdate(id, amount);
	}

	@Override
	public UserPoint findById(long id) {
		return this.userPointTable.selectById(id);
	}
}
