package com.monapp.auth.service;

import com.monapp.auth.model.User;
import com.monapp.auth.repository.userrepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private userrepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔐 Login utilisé par Spring Security
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // email comme identifiant
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    // 📝 Enregistrement d'un nouvel utilisateur
    public String register(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return "Email déjà utilisé.";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }

        user.setLoginCount(0); // initialiser loginCount à 0

        userRepository.save(user);
        return "Utilisateur enregistré avec succès !";
    }

    // 🔄 Incrémenter le nombre de connexions
    public void incrementLoginCount(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setLoginCount(user.getLoginCount() + 1);
            userRepository.save(user);
        }
    }

    // 📊 Compter le nombre total d'utilisateurs
    public long countUsers() {
        return userRepository.count();
    }

    // 📊 Compter le nombre d'administrateurs
    public long countAdmins() {
        return userRepository.countByRole("ROLE_ADMIN");
    }

    // 📊 Somme des connexions de tous les utilisateurs
    public long countTotalLogins() {
        return userRepository.findAll().stream()
                .mapToLong(User::getLoginCount)
                .sum();
    }

    // 📄 Liste de tous les utilisateurs
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ❌ Supprimer un utilisateur
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}
