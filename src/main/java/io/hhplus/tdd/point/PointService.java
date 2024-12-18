package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointTable userPointTable;

    // TODO 특정 유저의 포인트를 조회하는 기능
    UserPoint point(
            final long userId
    ) {
        return new UserPoint(0, 0, 0);
    }

    // TODO 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
    List<PointHistory> history(
            final long id
    ) {
        return List.of();
    }

    // TODO 특정 유저의 포인트를 충전하는 기능
    UserPoint charge(
            final long userId,
            final long amount
    ) {
        return new UserPoint(0, 0, 0);
    }

    // TODO 특정 유저의 포인트를 사용하는 기능
    UserPoint use(
            final long userId,
            final long amount
    ) {
        return new UserPoint(0, 0, 0);
    }
}
