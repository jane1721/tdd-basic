package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.ExceedingChargeException;
import io.hhplus.tdd.exception.InvalidChargeAmountException;
import io.hhplus.tdd.exception.InvalidUserIdException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class PointServiceTest {

    // Database Layer Mocking
    private final UserPointTable userPointTable = mock(UserPointTable.class);
    private final PointHistoryTable pointHistoryTable = mock(PointHistoryTable.class);

    private final PointService pointService = new PointService(userPointTable, pointHistoryTable);

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

    @Nested
    @DisplayName("history - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능")
    class HistoryTest {

        /**
         * 2. 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
         *  - 정상적인 사용자 ID 로 요청 시, 해당 사용자의 포인트 변동 내역 리스트를 반환한다.
         *  - 존재하지 않는 사용자 ID 로 요청 시, 빈 리스트를 반환한다.
         *  - 내역이 없는 사용자의 경우, 빈 리스트를 반환한다.
         */

        @DisplayName("정상적인 사용자 ID 로 요청 시, 해당 사용자의 포인트 데이터를 반환한다.")
        @Test
        void getUserPointHistorySuccess() throws Exception {
            // given
            long userId = 1L;
            List<PointHistory> expectedHistories = List.of(
                    new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                    new PointHistory(2L, userId, -500L, TransactionType.USE, System.currentTimeMillis())
            );
            given(pointHistoryTable.selectAllByUserId(userId)).willReturn(expectedHistories);

            // when
            List<PointHistory> actualHistories = pointService.history(userId);

            // then
            assertThat(actualHistories).isEqualTo(expectedHistories);
        }

        @DisplayName("존재하지 않는 사용자 ID 로 요청 시, 빈 리스트를 반환한다.") // Service 소스 수정 없는 테스트 케이스
        @Test
        void getNonUserPointHistorySuccess() throws Exception {
            // given
            final long userId = 999999999999L;
            given(pointHistoryTable.selectAllByUserId(userId)).willReturn(List.of());

            // when
            List<PointHistory> actualHistories = pointService.history(userId);

            // then
            assertThat(actualHistories).isEmpty();
        }

        @DisplayName("내역이 없는 사용자의 경우, 빈 리스트를 반환한다.") // Service 소스 수정 없는 테스트 케이스
        @Test
        void getEmptyPointHistorySuccess() throws Exception {
            // given
            long validId = 2L;
            given(pointHistoryTable.selectAllByUserId(validId)).willReturn(List.of());

            // when
            List<PointHistory> actualHistories = pointService.history(validId);

            // then
            assertThat(actualHistories).isEmpty();
        }
    }

    @Nested
    @DisplayName("charge - 특정 유저의 포인트를 충전하는 기능")
    class ChargeTest {

        /**
         * 3. 특정 유저의 포인트를 충전하는 기능
         *  - 잘못된 형식(자연수가 아닐 때)의 사용자 ID 로 요청 시 Exception 이 발생한다.
         *  - 충전 금액이 0 또는 음수일 때, Exception 이 발생한다.
         *  - 충전 금액이 1,000 미만일 때, Exception 이 발생한다.
         *  - 충전 후 포인트가 10,000,000 를 초과할 경우, Exception 이 발생한다.
         *  - 충전 내역을 저장하지 못 할 경우, Exception 이 발생한다.
         *  - 그 외 케이스의 경우(충전 금액이 0 보다 크고, 충전된 포인트가 10,000,000 미만일 때), 해당 사용자의 충전 후 포인트 데이터를 반환한다.
         */

        @DisplayName("잘못된 형식(자연수가 아닐 때)의 사용자 ID 로 요청 시 Exception 이 발생한다.")
        @Test
        void chargeNonUserPointFail() throws Exception {
            // given
            final long negativeUserId = -1L;
            final long zeroUserId = 0;

            // when
            // then
            assertThrows(
                    InvalidUserIdException.class,
                    () -> pointService.charge(negativeUserId, 1_000L)
            );

            assertThrows(
                    InvalidUserIdException.class,
                    () -> pointService.charge(zeroUserId, 1_000L)
            );
        }

        @DisplayName("충전 금액이 0 또는 음수일 때, Exception 이 발생한다.")
        @Test
        void chargeInvalidAmountFail() throws Exception {
            // given
            final long userId = 1L;
            final long negativeAmount = -1_000L;
            final long zeroAmount = 0L;

            // when
            // then
            assertThrows(
                    InvalidChargeAmountException.class,
                    () -> pointService.charge(userId, negativeAmount)
            );

            assertThrows(
                    InvalidChargeAmountException.class,
                    () -> pointService.charge(userId, zeroAmount)
            );
        }

        @DisplayName("충전 후 포인트가 10,000,000 를 초과할 경우, Exception 이 발생한다.")
        @Test
        void chargeExceedingPointFail() throws Exception {
            // given
            final long userId = 1L;
            final long originalUserPoint = 9_999_999L;
            final long chargeAmount = 2L;
            given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, originalUserPoint + chargeAmount, System.currentTimeMillis()));

            // when
            // then
            assertThrows(
                    ExceedingChargeException.class,
                    () -> pointService.charge(userId, chargeAmount)
            );
        }

        @DisplayName("그 외 케이스의 경우(충전 금액이 0 보다 크고, 충전된 포인트가 10,000,000 미만일 때), 해당 사용자의 충전 후 포인트 데이터를 반환한다.")
        @Test
        void chargeValidAmountSuccess() throws Exception {
            // given
            final long userId = 1L;
            final long originalAmount = 1_000L;
            final long chargeAmount = 5_000L;
            UserPoint expectedUserPoint = new UserPoint(userId, originalAmount + chargeAmount, System.currentTimeMillis());
            given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, originalAmount, System.currentTimeMillis()));
            given(userPointTable.insertOrUpdate(userId, originalAmount + chargeAmount)).willReturn(expectedUserPoint);

            // when
            UserPoint actualUserPoint = pointService.charge(userId, chargeAmount);

            // then
            assertThat(actualUserPoint).isEqualTo(expectedUserPoint);
        }
    }
}
