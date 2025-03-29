package io.hhplus.tdd.point.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl  implements  PointHistoryRepository {
	private final PointHistoryTable pointHistoryTable;
	@Override
	public PointHistory insert(long userId, long amount, TransactionType type) {
		return this.pointHistoryTable.insert(userId, amount, type, 100L);
	}

	@Override
	public List<PointHistory> findAllPointHistoriesByUserPointId(long userId) {
		return this.pointHistoryTable.selectAllByUserId(userId);
	}
}
