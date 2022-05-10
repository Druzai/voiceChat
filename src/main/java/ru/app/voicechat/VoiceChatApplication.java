package ru.app.voicechat;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.app.voicechat.models.Role;
import ru.app.voicechat.models.User;
import ru.app.voicechat.repositories.RoleRepository;
import ru.app.voicechat.repositories.UserRepository;
import ru.app.voicechat.services.UserService;

import java.util.List;
import java.util.Set;

@SpringBootApplication
public class VoiceChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoiceChatApplication.class, args);
    }

    @Bean
    public CommandLineRunner demoData(RoleRepository roleRepository, UserRepository userRepository,
                                      UserService userService) {
        if (roleRepository.findAll().size() == 0) {
            roleRepository.saveAll(List.of(
                    new Role(1L, "ROLE_ADMIN"),
                    new Role(2L, "ROLE_USER")
            ));
        }
        if (userRepository.findAll().isEmpty()) {
            var user = new User("admin", "P@ssw0rd");
            user.setRoles(Set.of(new Role(1L, "ROLE_ADMIN")));
            userService.save(user);
        }
        return args -> {
        };
    }
}
