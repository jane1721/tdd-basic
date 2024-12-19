package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserPointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    // 동시성 처리 테스트 - 동일 유저 포인트 충전/사용
    @DisplayName("동일한 사용자에 대해 충전과 사용 요청이 동시에 발생하는 상황")
    @Test
    void userPointConcurrencyTest() throws InterruptedException {
        // given
        final long userId = 1L;
        final long initialPoint = 10_000L;
        final int threadCount = 10;
        final long chargeAmount = 5_000L;
        final long useAmount = 3_000L;

        // 초기 사용자 포인트 설정
        userPointTable.insertOrUpdate(userId, initialPoint);

        // ThreadPool 을 이용하여 동시에 요청을 실행하기 위해 ExecutorService 설정
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // 모든 Thread 가 동시에 시작하고 종료될 때까지 기다리도록 동기화 하기 위해 CountDownLatch 설정
        CountDownLatch startLatch = new CountDownLatch(1); // Thread 시작 동기화
        CountDownLatch endLatch = new CountDownLatch(threadCount * 2); // Thread 작업 종료 대기

        /*
            충전과 사용 요청을 동시에 실행
            - charge 와 use 요청을 각각 다른 Thread 에서 실행한다.
            - charge 와 use 요청이 서로 독립적으로 병렬 실행하기 위해서이다.
         */
        for (int i = 0; i < threadCount; i++) {

            // 충전 요청 Thread
            executorService.execute(() -> {
                try {
                    // 모든 Thread 가 동시에 시작하도록 동기화
                    startLatch.await();

                    pointService.charge(userId, chargeAmount); // 포인트 충전

                } catch (InterruptedException e) {
                    // Thread 가 중단되었을 경우
                    Thread.currentThread().interrupt();
                } finally {
                    // Thread 종료 시 감소
                    endLatch.countDown();
                }
            });

            // 사용 요청 Thread
            executorService.execute(() -> {
                try {
                    // 모든 Thread 가 동시에 시작하도록 동기화
                    startLatch.await();

                    pointService.use(userId, useAmount); // 포인트 사용

                } catch (InterruptedException e) {
                    // Thread 가 중단되었을 경우
                    Thread.currentThread().interrupt();
                } finally {
                    // Thread 종료 시 감소
                    endLatch.countDown();
                }
            });
        }

        // 모든 Thread 가 작업을 시작할 때까지 대기
        startLatch.countDown();

        // 모든 Thread 가 작업을 완료할 때까지 대기
        endLatch.await();
        executorService.shutdown();

        // when
        UserPoint finalUserPoint = userPointTable.selectById(userId);
        long actualFinalPoint = finalUserPoint.point();

        // then
        // (초기 포인트) + (충전 횟수 * 충전 금액) - (사용 횟수 * 사용 금액)
        long expectedFinalPoint = initialPoint + (threadCount * chargeAmount) - (threadCount * useAmount);
        assertThat(actualFinalPoint).isEqualTo(expectedFinalPoint);
    }
}
