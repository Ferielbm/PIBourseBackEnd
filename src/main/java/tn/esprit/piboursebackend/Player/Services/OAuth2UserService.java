package tn.esprit.piboursebackend.Player.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Role;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Security.JwtUtils;

import java.util.Map;
import java.util.Collection;
import java.util.Collections;

/**
 * Service pour gérer l'authentification OAuth2 avec Google
 * - Crée un utilisateur s'il n'existe pas
 * - Connecte l'utilisateur existant
 * - Génère un JWT pour l'utilisateur
 */
@Service
@Transactional
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(oauth2User);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException("Erreur lors du traitement de l'utilisateur OAuth2");
        }
    }

    private OAuth2User processOAuth2User(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // Extraction des informations Google
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email non trouvé dans les informations Google");
        }

        // Vérifier si l'utilisateur existe déjà
        Player existingPlayer = playerRepository.findByEmail(email);
        
        if (existingPlayer != null) {
            // Utilisateur existant - mise à jour des informations si nécessaire
            updateExistingPlayer(existingPlayer, name, picture);
            return createOAuth2UserPrincipal(existingPlayer, attributes);
        } else {
            // Nouvel utilisateur - création
            Player newPlayer = createNewPlayer(email, name, picture);
            return createOAuth2UserPrincipal(newPlayer, attributes);
        }
    }

    private void updateExistingPlayer(Player player, String name, String picture) {
        // Mise à jour du nom d'utilisateur si nécessaire
        if (name != null && !name.isEmpty() && (player.getUsername() == null || player.getUsername().isEmpty())) {
            player.setUsername(name);
            playerRepository.save(player);
        }
    }

    private Player createNewPlayer(String email, String name, String picture) {
        Player newPlayer = Player.builder()
                .email(email)
                .username(name != null ? name : email.split("@")[0])
                .password("") // Pas de mot de passe pour les utilisateurs OAuth2
                .role(Role.ROLE_PLAYER)
                .build();
        
        return playerRepository.save(newPlayer);
    }

    private OAuth2User createOAuth2UserPrincipal(Player player, Map<String, Object> attributes) {
        // Ajouter le JWT dans les attributs pour qu'il soit accessible
        String jwt = jwtUtils.generateJwtToken(player.getEmail());
        attributes.put("jwt", jwt);
        attributes.put("playerId", player.getId());
        attributes.put("playerRole", player.getRole().name());
        
        return new CustomOAuth2User(attributes, player);
    }

    /**
     * Classe interne pour représenter l'utilisateur OAuth2 avec les informations du Player
     */
    public static class CustomOAuth2User implements OAuth2User {
        private final Map<String, Object> attributes;
        private final Player player;

        public CustomOAuth2User(Map<String, Object> attributes, Player player) {
            this.attributes = attributes;
            this.player = player;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public String getName() {
            return player.getUsername();
        }

        public Player getPlayer() {
            return player;
        }

        public String getJwt() {
            return (String) attributes.get("jwt");
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER"));
        }
    }
}
