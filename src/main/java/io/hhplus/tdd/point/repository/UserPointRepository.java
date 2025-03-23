package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;

public interface UserPointRepository  {
	UserPoint save(long id, long amount);
	UserPoint findById(long id);
}
