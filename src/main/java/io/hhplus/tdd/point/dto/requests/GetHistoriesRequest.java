package io.hhplus.tdd.point.dto.requests;

import io.hhplus.tdd.exceptions.CustomInvalidRequestException;
import io.hhplus.tdd.point.domain.ErrorCode;

public record GetHistoriesRequest(long id) {
	public GetHistoriesRequest {
		if(id <= 0) throw new CustomInvalidRequestException(ErrorCode.ID_POSITIVE_NUMBER_POLICY);
	}
}
