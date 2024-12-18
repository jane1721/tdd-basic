package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.InvalidUserIdException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class PointServiceTest {

    private final UserPointTable userPointTable = mock(UserPointTable.class); // Database Layer Mocking
    private final PointService pointService = new PointService(userPointTable);

    @Nested
    @DisplayName("point - 특정 유저의 포인트를 조회하는 기능")
    class PointTest {

        /**
         * 1. 특정 유저의 포인트를 조회하는 기능
         *  - 정상적인 사용자 ID 로 요청 시, 해당 사용자의 포인트 데이터를 반환한다.
         *  - 존재하지 않는 사용자 ID 로 요청 시, 포인트 0 인 데이터를 반환한다.
         *  - 잘못된 형식(자연수가 아닐 때)의 사용자 ID 로 요청 시 Exception 이 발생한다.
         */

        @DisplayName("정상적인 사용자 ID 로 요청 시, 해당 사용자의 포인트 데이터를 반환한다.")
        @Test
        void getUserPointSuccess() throws Exception {
            // given
            final long userId = 1L;
            UserPoint expectedUserPoint = new UserPoint(userId, 5000L, System.currentTimeMillis());
            given(userPointTable.selectById(userId)).willReturn(expectedUserPoint);

            // when
            UserPoint actualUserPoint = pointService.point(userId);

            // then
            Assertions.assertEquals(expectedUserPoint, actualUserPoint);
        }

        @DisplayName("존재하지 않는 사용자 ID 로 요청 시 포인트 0 인 데이터를 반환한다.") // Service 소스 수정 없는 테스트 케이스
        @Test
        void getNonUserPointSuccess() throws Exception {
            // given
            final long userId = 999999999999L;
            UserPoint expectedUserPoint = new UserPoint(userId, 0L, System.currentTimeMillis());
            given(userPointTable.selectById(userId)).willReturn(expectedUserPoint);

            // when
            UserPoint actualUserPoint = pointService.point(userId);

            // then
            Assertions.assertEquals(expectedUserPoint, actualUserPoint);
        }

        @DisplayName("잘못된 형식(자연수가 아닐 때)의 사용자 ID 로 요청 시 Exception 이 발생한다.")
        @Test
        void getInvalidUserPointFail() throws Exception {
            // given
            final long negativeUserId = -1L;
            final long zeroUserId = 0;

            // when
            // then
            assertThrows(
                    InvalidUserIdException.class,
                    () -> pointService.point(negativeUserId)
            );

            assertThrows(
                    InvalidUserIdException.class,
                    () -> pointService.point(zeroUserId)
            );
        }
    }
}
