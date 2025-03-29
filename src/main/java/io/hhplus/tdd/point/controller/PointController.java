package io.hhplus.tdd.point.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.requests.ChargeRequest;
import io.hhplus.tdd.point.dto.requests.ChargeRequestBody;
import io.hhplus.tdd.point.dto.requests.GetHistoriesRequest;
import io.hhplus.tdd.point.dto.requests.GetPointRequest;
import io.hhplus.tdd.point.dto.requests.UseRequest;
import io.hhplus.tdd.point.dto.requests.UseRequestBody;
import io.hhplus.tdd.point.dto.responses.ChargeResponse;
import io.hhplus.tdd.point.dto.responses.GetPointHistoriesResponse;
import io.hhplus.tdd.point.dto.responses.GetPointResponse;
import io.hhplus.tdd.point.dto.responses.UseResponse;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.service.PointServiceImpl;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    @GetMapping("{id}")
    public GetPointResponse point(
            @PathVariable("id") long id
    ) {
        return this.pointService.getPoint(new GetPointRequest(id));
    }

    @GetMapping("{id}/histories")
    public GetPointHistoriesResponse history(
            @PathVariable("id") long id
    ) {
        return this.pointService.getPointHistories(new GetHistoriesRequest(id));
    }


    @PatchMapping("{id}/charge")
    public ChargeResponse charge(
            @PathVariable("id") long id,
            @RequestBody ChargeRequestBody requestBody
    ) {
        return this.pointService.charge(new ChargeRequest(id, requestBody.amount()));
    }


    @PatchMapping("{id}/use")
    public UseResponse use(
            @PathVariable("id") long id,
            @RequestBody UseRequestBody requestBody
    ) {
        return this.pointService.use(new UseRequest(id, requestBody.amount()));
    }
}
