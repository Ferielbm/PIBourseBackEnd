package tn.esprit.piboursebackend.Portfolio.Controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Portfolio.Entity.RiskEngine;
import tn.esprit.piboursebackend.Portfolio.service.RiskEngineService;

import java.util.List;

@RestController
@RequestMapping("/RiskEngines")
public class RiskEngineController {

    private final RiskEngineService RiskEngineService;

    public RiskEngineController(RiskEngineService RiskEngineService) {
        this.RiskEngineService = RiskEngineService;
    }

    @GetMapping
    public List<RiskEngine> getAllRiskEngines() {
        return RiskEngineService.getAllRiskEngines();
    }

    @PostMapping
    public RiskEngine createRiskEngine(@RequestBody RiskEngine RiskEngine) {
        return RiskEngineService.createRiskEngine(RiskEngine);
    }

}
