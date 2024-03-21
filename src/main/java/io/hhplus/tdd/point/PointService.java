package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final UserPointTable userPointTable; // 유저 포인트 테이블
    private final PointHistoryTable pointHistoryTable; // 포인트 내역 테이블

    @Autowired
    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    // synchronized : 동기화 메소드로 구현하여 여러 스레드가 동시에 접근하지 못하도록 함
    // 특정 유저의 포인트 조회 기능
    public synchronized UserPoint getByUserId(Long userId) {
        return userPointTable.selectById(userId);
    }

    // 특정 유저의 포인트 충전/이용 내역 조회 기능
    public synchronized List<PointHistory> getHistoriesByUserId(Long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    // 특정 유저의 포인트 충전 기능
    public synchronized UserPoint charge(Long userId, Long amount) {
        UserPoint userPoint = userPointTable.selectById(userId);
        long result = userPoint.point() + amount;
        if (result >= 100000) {
            throw new RuntimeException("100,000 포인트를 초과하였습니다.");
        }
        return userPointTable.insertOrUpdate(userId, result);
    }

    // 특정 유저의 포인트 사용 기능
    public synchronized UserPoint use(Long userId, Long amount) {
        UserPoint userPoint = userPointTable.selectById(userId);
        long currentBalance = userPoint.point();
        if (currentBalance < amount) {
            throw new RuntimeException("잔고가 부족하여 포인트를 사용할 수 없습니다.");
        }
        long result = currentBalance - amount;
        return userPointTable.insertOrUpdate(userId, result);
    }

}

