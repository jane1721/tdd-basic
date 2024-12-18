package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;

    /**
     * 특정 유저의 포인트를 조회
     *
     * @param id    유저 ID
     * @return      유저 포인트
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        return pointService.point(id);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회
     *
     * @param id    유저 ID
     * @return      유저 포인트 충전/이용 내역
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        return pointService.history(id);
    }

    /**
     * 특정 유저의 포인트를 충전
     *
     * @param id        유저 ID
     * @param amount    충전할 포인트
     * @return          충전 후 유저 포인트
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        return pointService.charge(id, amount);
    }

    /**
     * 특정 유저의 포인트를 사용
     *
     * @param id        유저 ID
     * @param amount    사용할 포인트
     * @return          사용 후 유저 포인트
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        return pointService.use(id, amount);
    }
}
