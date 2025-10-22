package tn.esprit.piboursebackend.Portfolio.service;

import tn.esprit.piboursebackend.Portfolio.Entity.RiskEngine;

import java.util.List;

public interface IRiskEngineService {
    RiskEngine createRiskEngine(RiskEngine RiskEngine);
    RiskEngine getRiskEngineById(int id);
    List<RiskEngine> getAllRiskEngines();
    RiskEngine updateRiskEngine(int id, RiskEngine RiskEngine);
    void deleteRiskEngine(int id);
}
