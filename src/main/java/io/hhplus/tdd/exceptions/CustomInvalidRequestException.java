package io.hhplus.tdd.exceptions;

import io.hhplus.tdd.point.domain.ErrorCode;

public class CustomInvalidRequestException extends  IllegalArgumentException {
	public CustomInvalidRequestException(ErrorCode errorCode) {
		super(errorCode.getMessage());
	}
}
