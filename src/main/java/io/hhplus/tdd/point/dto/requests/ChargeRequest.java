package io.hhplus.tdd.point.dto.requests;

import static io.hhplus.tdd.point.domain.UserPoint.*;

import io.hhplus.tdd.exceptions.CustomInvalidRequestException;
import io.hhplus.tdd.point.domain.ErrorCode;


public record ChargeRequest (long id, long amount){
	public ChargeRequest {
		if(id <= 0) throw new CustomInvalidRequestException(ErrorCode.ID_POSITIVE_NUMBER_POLICY);
		if(amount < MIN_CHARGE_POINT_AMOUNT) throw new CustomInvalidRequestException(ErrorCode.MIN_CHARGE_AMOUNT_VALUE_POLICY);
		if(amount > MAX_CHARGE_POINT_AMOUNT) throw new CustomInvalidRequestException(ErrorCode.MAX_CHARGE_AMOUNT_VALUE_POLICY);
	}
}
