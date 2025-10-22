package tn.esprit.piboursebackend.Portfolio.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Portfolio.Dto.RecordCashFlowRequest;
import tn.esprit.piboursebackend.Portfolio.Dto.TransferCashRequest;
import tn.esprit.piboursebackend.Portfolio.Entity.CashFlow;
import tn.esprit.piboursebackend.Portfolio.service.CashService;


@RestController
@RequestMapping("/Cash")
public class CashController {

    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping("/record")
    public ResponseEntity<CashFlow> record(@RequestBody RecordCashFlowRequest req) {
        var saved = cashService.recordCashFlow(
                req.portfolioId(), req.amount(), req.currency(), req.type(), req.asOf()
        );
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestBody TransferCashRequest req) {
        cashService.transferCashBetween(
                req.fromPortfolioId(), req.toPortfolioId(), req.amount(), req.currency(), req.asOf()
        );
        return ResponseEntity.ok().build();
    }
}
