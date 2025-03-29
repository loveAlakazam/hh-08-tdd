package io.hhplus.tdd.point.dto.responses;

import io.hhplus.tdd.point.domain.UserPoint;

public record GetPointResponse(long id, long point) {
	public static GetPointResponse from(UserPoint entity) {
		return new GetPointResponse(entity.id(), entity.point());
	}
}
