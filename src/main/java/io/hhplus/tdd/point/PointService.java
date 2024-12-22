package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>(); // 유저 ID 별로 Lock 을 관리

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

        ReentrantLock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
        lock.lock(); // 유저 락 설정

        try {
            UserPoint originalUserPoint = userPointTable.selectById(userId);

            if (originalUserPoint.point() + amount > 10_000_000) {
                throw new ExceedingChargeException();
            }

            long updatedPoint = originalUserPoint.point() + amount;

            pointHistoryTable.insert(userId, updatedPoint, TransactionType.CHARGE, System.currentTimeMillis());

            return userPointTable.insertOrUpdate(userId, updatedPoint);

        } finally {
            lock.unlock(); // 유저 락 해제
        }
    }

    /**
     * 특정 유저의 포인트를 사용하는 기능
     *
     * @param userId    유저 ID
     * @param amount    사용할 포인트
     * @return          사용 후 유저 포인트
     */
    UserPoint use(
            final long userId,
            final long amount
    ) {
        if (userId <= 0) {
            throw new InvalidUserIdException();
        }

        if (amount <= 0) {
            throw new InvalidUseAmountException();
        }

        ReentrantLock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
        lock.lock(); // 유저 락 설정

        try {
            UserPoint originalUserPoint = userPointTable.selectById(userId);

            long updatedPoint = originalUserPoint.point() - amount;

            if (updatedPoint < 0) {
                throw new ExceedingUseException();
            }

            pointHistoryTable.insert(userId, updatedPoint, TransactionType.USE, System.currentTimeMillis());

            return userPointTable.insertOrUpdate(userId, updatedPoint);

        } finally {
            lock.unlock(); // 유저 락 해제
        }
    }
}
