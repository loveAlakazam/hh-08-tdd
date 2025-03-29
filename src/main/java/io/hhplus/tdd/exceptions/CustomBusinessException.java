package io.hhplus.tdd.exceptions;

import io.hhplus.tdd.point.domain.ErrorCode;

public class CustomBusinessException extends RuntimeException{
	public CustomBusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
	}
}
