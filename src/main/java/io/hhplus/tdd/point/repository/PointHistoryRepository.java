package io.hhplus.tdd.point.repository;

import java.util.List;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;

public interface PointHistoryRepository {
	PointHistory insert(long userId, long amount, TransactionType type);
	List<PointHistory> findAllPointHistoriesByUserPointId(long userId);
}
