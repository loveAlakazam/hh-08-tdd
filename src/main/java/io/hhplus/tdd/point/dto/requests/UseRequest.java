package io.hhplus.tdd.point.dto.requests;

import static io.hhplus.tdd.point.domain.UserPoint.*;

import io.hhplus.tdd.exceptions.CustomInvalidRequestException;
import io.hhplus.tdd.point.domain.ErrorCode;

public record UseRequest(long id, long amount) {
	public UseRequest {
		if(id <= 0) throw new CustomInvalidRequestException(ErrorCode.ID_POSITIVE_NUMBER_POLICY);
		if(amount < MIN_USE_POINT_AMOUNT) throw new CustomInvalidRequestException(ErrorCode.MIN_USE_AMOUNT_VALUE_POLICY);
		if(amount > MAX_USE_POINT_AMOUNT) throw new CustomInvalidRequestException(ErrorCode.MAX_USE_AMOUNT_VALUE_POLICY);
	}
}
