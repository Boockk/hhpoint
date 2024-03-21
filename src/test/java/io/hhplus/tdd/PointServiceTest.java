package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PointServiceTest {
    private final UserPointTable userPointTable = new UserPointTable();
    private final PointHistoryTable pointHistoryTable = new PointHistoryTable();
    private final PointService pointService = new PointService(userPointTable, pointHistoryTable);

    // 특정 유저의 포인트 조회 기능 테스트
    @Test
    void getByUserId() {
        // 입력값
        long userId = 1L;
        userPointTable.insertOrUpdate(userId, 5000L);

        // 실행
        UserPoint result = pointService.getByUserId(userId);

        // 검증
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(5000L);
    }

    // 특정 유저의 포인트 충전/이용 내역 조회 기능 테스트
    @Test
    void getHistoriesByUserId() {
        // 입력값
        long userId = 1L;
        long amount = 1000L;
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        // 실행
        List<PointHistory> histories = pointService.getHistoriesByUserId(userId);

        // 검증
        // 포인트 내역이 2개가 있는가?
        assertThat(histories).hasSize(2);
        // 포인트 내역의 유저 ID가 주어진 유저 ID와 일치하는가?
        assertThat(histories).allMatch(history -> history.userId() == userId);
    }

    // 특정 유저의 포인트 충전 기능 테스트
    @Test
    void charge() {
        // 입력값
        long userId = 1L;
        userPointTable.insertOrUpdate(userId, 5000L);
        long amount = 3000L;

        // 실행
        UserPoint result = pointService.charge(userId, amount);

        // 검증
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(8000L);
    }

    // 포인트 충전 기능의 실패 테스트 - 사용자의 잔액이 100,000을 초과하는 경우
    @Test
    void fail_IfUsersPointOver100000() {
        // 입력값
        long userId = 1L;
        userPointTable.insertOrUpdate(userId, 99000L);
        long amount = 2000L;

        // 검증
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용자의 잔액은 100,000 포인트 초과 불가능 합니다.");
    }
}
