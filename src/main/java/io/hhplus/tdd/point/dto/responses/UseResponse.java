package io.hhplus.tdd.point.dto.responses;

import io.hhplus.tdd.point.domain.UserPoint;

public record UseResponse(long id, long point) {
	public static UseResponse from (UserPoint entity) {
		return new UseResponse(entity.id(), entity.point());
	}
}
