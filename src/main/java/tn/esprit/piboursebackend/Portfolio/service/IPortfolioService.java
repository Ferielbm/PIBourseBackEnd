package tn.esprit.piboursebackend.Portfolio.service;


import tn.esprit.piboursebackend.Portfolio.Entity.Portfolio;

import java.util.List;

public interface IPortfolioService {
    Portfolio createPortfolio(Portfolio Portfolio);
    Portfolio getPortfolioById(Long id);
    List<Portfolio> getAllPortfolios();
    Portfolio updatePortfolio(Long id, Portfolio Portfolio);
    void deletePortfolio(Long id);
}
