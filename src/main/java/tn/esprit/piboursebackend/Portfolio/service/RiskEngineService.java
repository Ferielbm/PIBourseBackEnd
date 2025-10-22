package tn.esprit.piboursebackend.Portfolio.service;

import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Portfolio.Entity.RiskEngine;
import tn.esprit.piboursebackend.Portfolio.Repository.RiskEngineRepository;

import java.util.List;

@Service
public class RiskEngineService {
    private final RiskEngineRepository RiskEngineRepository;

    public RiskEngineService(RiskEngineRepository RiskEngineRepository) {
        this.RiskEngineRepository = RiskEngineRepository;
    }

    public List<RiskEngine> getAllRiskEngines() {
        return RiskEngineRepository.findAll();
    }

    public RiskEngine createRiskEngine(RiskEngine RiskEngine) {
        return RiskEngineRepository.save(RiskEngine);
    }
}
