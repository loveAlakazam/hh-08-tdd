package io.hhplus.tdd.point.domain;

import io.hhplus.tdd.exceptions.CustomBusinessException;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    /**
     * UserPoint 정책
     *
     * - MIN_CHARGE_POINT_AMOUNT: 최소 포인트 충전금액
     * - MAX_CHARGE_POINT_AMOUNT: 최대 포인트 충전금액
     *
     * - MIN_USE_POINT_AMOUNT: 최소 포인트 사용금액
     * - MAX_USE_POINT_AMOUNT: 최대 포인트 사용금액
     */
    public static int MIN_CHARGE_POINT_AMOUNT = 1000;
    public static int MAX_CHARGE_POINT_AMOUNT = 50000;

    public static int MIN_USE_POINT_AMOUNT = 100;
    public static int MAX_USE_POINT_AMOUNT = 50000;


    // 포인트 충전에 대한 책임 부여
    public long charge(long amount) {
        return  this.point + amount; // 포인트 충전
    }

    // 포인트 사용에 대한 책임 부여
    public long use(long amount) {
        // 잔고가 사용금액보다 부족할 경우 CustomBusinessException 예외처리
        if(amount > this.point) {
            throw new CustomBusinessException(ErrorCode.OVER_USE_AMOUNT_VALUE_THAN_BALANCE_POLICY);
        }
       return this.point - amount; // 포인트 사용
    }
}
