# 동시성 제어 방식에 대한 분석 및 보고서 작성

## 1. 개요

동일한 유저에 대해 포인트 충전과 사용 요청이 동시에 발생하였을 때, 데이터 무결성을 보장해야 하는 요구사항이 있었습니다. 그래서 다수의 Thread 로 동시에 접근하는 환경에서 한번에 하나씩 요청을 처리하기 위한 여러 동시성 처리 방식에 대하여 학습하였고, 단일 인스턴스 환경에서의 동시성 제어에 적합하다고 생각되는 방법을 통해 동시성 제어를 구현하였습니다.

## 2. 동시성 문제

동시성 문제는 여러 Thread 가 동시에 같은 데이터에 접근하거나 수정할 때 발생할 수 있습니다. 해당 케이스에서 동시성 제어가 처리되지 않는다면 **경쟁 조건**(race condition)과 **데이터 불일치**(data inconsistency) 문제가 발생할 수 있다고 파악하였습니다.

- **경쟁 조건(Race Condition)**: 여러 Thread 가 동시에 같은 자원을 수정하려 할 때, 자원의 상태가 예기치 않게 변경되는 문제
- **데이터 불일치(Data Inconsistency)**: 여러 Thread 가 동시에 데이터를 읽고 수정하여, 최종적으로 데이터가 일관되지 않게 되는 문제

이 문제들은 특히 **포인트 충전** 및 **포인트 사용**과 같은 **트랜잭션 처리**가 동시에 발생할 때 발생할 수 있습니다. 예를 들어, 포인트를 충전하는 작업과 사용 작업이 동시에 발생할 때 포인트의 정확한 계산을 보장하기 위해(`Thread-safe`한 로직을 구현하기 위해) 적절한 동시성 제어가 필요합니다.

## 3. 동시성 제어 기술 선택 과정

동시성 제어를 위해 사용 가능한 여러 가지 기술을 학습하고, 각 기술의 장단점을 비교한 뒤 ReentrantLock을 사용하기로 결정하였습니다. 기술 선택 과정에서 고려된 주요 사항은 다음과 같습니다:

### 3.1. Java 의 synchronized 키워드
- **특징**
  - Java 에서 기본적으로 제공되는 동기화 방법으로, 코드 구현이 간편합니다.
  - JVM 수준에서 동기화 메커니즘을 지원합니다.
  - **동기화 블록**을 사용하여 **동시성 제어**를 수행하므로, 락의 범위가 넓어질 수 있습니다. 이는 성능 저하를 일으킬 수 있습니다.
  - 객체 단위의 잠금으로 동작하여 유저별 개별적인 잠금을 구현하기 어렵고, 유연성이 떨어집니다.

### 3.2. Java 의 ReentrantLock
- **특징**
  - Java 의 `concurrent` 패키지에 있는 `ReentrantLock`을 사용하여 동시성 제어를 수행합니다.
  - **Lock 인터페이스**를 구현한 클래스로, `lock()`과 `unlock()` 메서드를 사용하여 동시성 제어를 수행합니다.
  - 동기화 블록보다 유연하게 사용할 수 있으며, **조건 변수**를 사용하여 **동시성 제어**를 수행할 수 있습니다.
  - 명시적으로 lock 과 unlock 을 관리해야 하므로 코드가 복잡해질 수 있습니다.

### 3.3. Phaser 와 Semaphore

- **Phaser**: 다수의 스레드가 단계적으로 작업을 완료할 수 있도록 동기화를 지원하지만, 본 문제와 같은 개별 사용자 단위 동기화에는 적합하지 않습니다.
- **Semaphore**: 공유 리소스 접근에 대한 제한된 허용을 제공하나, 본 문제처럼 특정 사용자 단위로 동기화를 구현하기엔 부적합.

### 3.4. Spring/Spring Boot 에서의 트랜잭션 관리

Spring 에서는 `@Transactional`과 같은 선언적 트랜잭션 관리로 데이터베이스 트랜잭션의 동시성 문제를 해결할 수 있습니다. 하지만 본 문제는 단일 데이터베이스 트랜잭션 수준을 넘어 애플리케이션 계층에서 유저별 동시성을 제어해야 하므로 추가적인 동기화 메커니즘이 필요했습니다.

### 3.5 분산 환경에서의 동시성 제어

분산 환경에서는 단일 애플리케이션 인스턴스의 ReentrantLock 사용만으로는 동시성 문제를 해결할 수 없습니다.  
이를 해결하기 위해 다음 기술을 검토하였습니다:

- **Redis 의 분산 락**: 여러 인스턴스 간 동기화를 보장할 수 있는 기술로, SETNX 와 EXPIRE 명령어를 활용하여 구현할 수 있습니다.
- **ZooKeeper**: 분산 락 관리 및 노드 간 데이터 일관성을 유지하기 위한 고급 분산 시스템입니다.

## 4. 선택한 동시성 제어 방식

Java 의 `concurrent` 패키지에 있는 **ReentrantLock**을 사용하는 방법을 설명합니다.

### 4.1. Lock 을 활용한 동시성 제어

- **ReentrantLock**: 자바의 `ReentrantLock`을 사용하여 동일한 리소스에 대한 접근을 순차적으로 처리하도록 합니다. `lock.lock()`으로 자원을 잠그고, 작업이 끝난 후 `lock.unlock()`으로 자원을 해제합니다.

  이 방식은 **Thread 가 리소스를 독점적으로 접근할 수 있도록 보장**하며, **동시성 문제**를 해결합니다.

### 4.2. 동기화된 실행을 위한 CountDownLatch

- **CountDownLatch**는 여러 Thread 가 동기화되어 실행될 수 있도록 하기 위한 도구입니다. 이 테스트에서는 여러 Thread 가 동시에 포인트를 충전하고 사용하는 작업을 진행할 때, 모든 Thread 가 시작하기 전에 동기화되도록 `CountDownLatch`를 사용하였습니다.

  이를 통해 각 Thread 가 **동일한 시점에 작업을 시작하도록** 보장하고, **모든 작업이 종료될 때까지 대기**하여, 동시성 제어가 제대로 이루어졌는지 확인합니다.

## 5. 동시성 제어 구현

### 5.1. 포인트 충전 및 사용 처리

포인트 충전과 사용 요청을 처리하는 메서드는 아래와 같습니다:

`PointService.java`
```java
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
```
`UserPointServiceConcurrencyTest.java`
```java
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
```

### 5.2. 동시성 제어 기능 통합 테스트

- **테스트 설정**: `ExecutorService`를 사용하여 다수의 Thread 에서 동시에 포인트 충전과 사용 요청을 처리하는 테스트를 구현하였습니다. 각 Thread 는 충전 또는 사용 요청을 독립적으로 실행하고, `CountDownLatch`를 사용하여 모든 Thread 가 시작한 후에 동시에 작업을 시작하고 완료하도록 동기화 하였습니다.

```java
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
```

## 6. 결론

동시성 제어는 Multithreading 환경에서 데이터의 일관성을 보장하고 경쟁 조건(Race Condition)을 방지하는 데 필수적인 요소입니다. 이번 구현에서는 `ReentrantLock`을 사용하여 포인트 충전과 사용에 대한 동시성 문제를 해결해 보았고, `CountDownLatch`를 이용하여 통합 테스트에서 동시성 처리가 제대로 이루어지는지 확인하였습니다. 이를 통해 동일 유저의 동시 요청에 대하여 정확성을 보장할 수 있었습니다.

## 7. 참고 자료

- [자바 Concurrency Utilities](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html)
- [ReentrantLock](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/ReentrantLock.html)
- [CountDownLatch](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CountDownLatch.html)