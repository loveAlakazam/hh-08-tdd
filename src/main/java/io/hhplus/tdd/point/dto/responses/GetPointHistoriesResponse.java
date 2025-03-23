package io.hhplus.tdd.point.dto.responses;

import java.util.List;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;

public record GetPointHistoriesResponse(List<UnitPointHistory> histories) {
	public record UnitPointHistory (long id, long userId, long amount, TransactionType type) {
		public static UnitPointHistory from(PointHistory entity) {
			return new UnitPointHistory(entity.id(), entity.userId(), entity.amount(), entity.type());
		}
	}

	public static GetPointHistoriesResponse from (List<PointHistory> entities) {
		List<UnitPointHistory> responses = entities.stream().map(UnitPointHistory::from).toList();
		return new GetPointHistoriesResponse(responses);
	}
}
