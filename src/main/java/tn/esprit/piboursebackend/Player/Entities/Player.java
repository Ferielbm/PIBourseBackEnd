package tn.esprit.piboursebackend.Player.Entities;

import jakarta.persistence.*;
import tn.esprit.piboursebackend.Credit.Entity.Loan;
import tn.esprit.piboursebackend.Portfolio.Entity.Portfolio;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    public int getTotalCreditsTaken() {
        return totalCreditsTaken;
    }

    public void setTotalCreditsTaken(int totalCreditsTaken) {
        this.totalCreditsTaken = totalCreditsTaken;
    }

    private int totalCreditsTaken = 0;
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    private Wallet wallet;

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }


    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios = portfolios;
    }

    // 🔗 Un joueur peut avoir plusieurs portefeuilles
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Portfolio> portfolios = new ArrayList<>();

    // 🔗 Un joueur peut avoir plusieurs prêts
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans = new ArrayList<>();
    public List<Loan> getLoans() {
        return loans;
    }

    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
}
