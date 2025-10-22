package tn.esprit.piboursebackend.Player.Security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.esprit.piboursebackend.Player.Entities.Player;

import java.util.Collection;
import java.util.Collections;

/**
 * Implémentation de UserDetails pour Spring Security
 * Représente un utilisateur authentifié
 */
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructeur
     */
    public UserDetailsImpl(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Construit un UserDetailsImpl à partir d'un Player
     * @param player l'entité Player
     * @return UserDetailsImpl
     */
    public static UserDetailsImpl build(Player player) {
        // Convertir le rôle en GrantedAuthority
        GrantedAuthority authority = new SimpleGrantedAuthority(player.getRole().name());

        return new UserDetailsImpl(
                player.getId(),
                player.getUsername(),
                player.getEmail(),
                player.getPassword(),
                Collections.singletonList(authority)
        );
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Méthodes de UserDetails - toutes retournent true par défaut
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

