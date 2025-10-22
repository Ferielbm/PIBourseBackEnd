package tn.esprit.piboursebackend.Player.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;

/**
 * Service pour charger les détails de l'utilisateur lors de l'authentification
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * Charge un utilisateur par son username
     * @param username le nom d'utilisateur
     * @return UserDetails
     * @throws UsernameNotFoundException si l'utilisateur n'existe pas
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(player);
    }
}

