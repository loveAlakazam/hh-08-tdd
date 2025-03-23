package io.hhplus.tdd.point.dto.responses;

import io.hhplus.tdd.point.domain.UserPoint;

public record ChargeResponse(long id, long point) {
	public static ChargeResponse from(UserPoint entity) {

		return new ChargeResponse(entity.id(), entity.point());
	}
}
