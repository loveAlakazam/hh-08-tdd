package io.hhplus.tdd.point.domain;

import static io.hhplus.tdd.point.domain.UserPoint.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	/**
	 * Validation(유효성검사) 에러코드
	 * : CustomInvalidRequestException 예외 메시지에 활용됩니다.
	 */
	ID_POSITIVE_NUMBER_POLICY("id 는 양수 여야 합니다."),
	AMOUNT_POSITIVE_NUMBER_POLICY("포인트값(amount)은 양수 여야 합니다."),

	MIN_CHARGE_AMOUNT_VALUE_POLICY("충전금액은 최소 "+ MIN_CHARGE_POINT_AMOUNT+ "원 이상이어야 합니다."),
	MAX_CHARGE_AMOUNT_VALUE_POLICY("최대 충전금액은 "+ MAX_CHARGE_POINT_AMOUNT+ "원 입니다."),

	MIN_USE_AMOUNT_VALUE_POLICY("사용금액은 최소 "+ MIN_USE_POINT_AMOUNT+ "원 이상이어야 합니다."),
	MAX_USE_AMOUNT_VALUE_POLICY("최대 사용금액은 "+ MAX_USE_POINT_AMOUNT+ "원 입니다."),

	/**
	 * 비즈니스로직 에러코드
	 * : CustomBusinessException 예외 메시지에 활용됩니다.
	 */
	OVER_USE_AMOUNT_VALUE_THAN_BALANCE_POLICY("잔고금액을 초과한 사용금액입니다.")
	;

	private final String message;
}
