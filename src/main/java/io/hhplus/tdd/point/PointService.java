package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.ExceedingChargeException;
import io.hhplus.tdd.exception.InvalidChargeAmountException;
import io.hhplus.tdd.exception.InvalidUserIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    /**
     * 특정 유저의 포인트를 조회하는 기능
     *
     * @param userId    유저 ID
     * @return          유저 포인트 객체
     */
    UserPoint point(
            final long userId
    ) {
        if (userId <= 0) {
            throw new InvalidUserIdException();
        }

        return userPointTable.selectById(userId);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
     *
     * @param userId    유저 ID
     * @return          유저 포인트 충전/이용 내역 리스트
     */
    List<PointHistory> history(
            final long userId
    ) {
        return pointHistoryTable.selectAllByUserId(userId);
    }


    /**
     * 특정 유저의 포인트를 충전하는 기능
     *
     * @param userId    유저 ID
     * @param amount    충전할 포인트
     * @return          충전 후 유저 포인트
     */
    UserPoint charge(
            final long userId,
            final long amount
    ) {

        if (userId <= 0) {
            throw new InvalidUserIdException();
        }

        if (amount <= 0) {
            throw new InvalidChargeAmountException();
        }

        UserPoint originalUserPoint = userPointTable.selectById(userId);

        if (originalUserPoint.point() + amount > 10_000_000) {
            throw new ExceedingChargeException();
        }

        long updatedPoint = originalUserPoint.point() + amount;

        pointHistoryTable.insert(userId, updatedPoint, TransactionType.CHARGE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(userId, updatedPoint);
    }

    // TODO 특정 유저의 포인트를 사용하는 기능
    UserPoint use(
            final long userId,
            final long amount
    ) {
        return new UserPoint(0, 0, 0);
    }
}
