package io.hhplus.tdd.point.domain;

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
}
