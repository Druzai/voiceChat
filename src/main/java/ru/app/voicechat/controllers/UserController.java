package ru.app.voicechat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.app.voicechat.components.UserValidator;
import ru.app.voicechat.models.Role;
import ru.app.voicechat.models.User;
import ru.app.voicechat.services.RoleService;
import ru.app.voicechat.services.SecurityService;
import ru.app.voicechat.services.UserService;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserValidator userValidator;

    @GetMapping("/registration")
    public String registration(Model model) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        }

        var usr = new User();
        usr.setRoles(Set.of(roleService.getUserRole().get()));
        model.addAttribute("userForm", usr);

        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("userForm") User userForm, BindingResult bindingResult, Model model) {
        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "registration";
        }
        userService.save(userForm);

        securityService.autoLogin(userForm.getUsername(), userForm.getPasswordConfirm());

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login(Model model, String error, String logout) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        }

        if (error != null)
            model.addAttribute("error", "Ваш логин и/или пароль не верны.");

        if (logout != null)
            model.addAttribute("message", "Вы успешно вышли из аккаунта.");

        return "login";
    }

    @GetMapping("/me")
    public String getUser(Model model) {
        var user = userService.getUser();
        model.addAttribute("userRole", user.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));
        model.addAttribute("allRoles", roleService.getRoles());
        model.addAttribute("userForm", user);
        return "user";
    }

    @PostMapping("/me")
    public String setUserRole(@ModelAttribute("userForm") User userForm, Model model) {
        var user = userService.getUser();
        user.setRoles(userForm.getRoles());
        userService.save(user);
        model.addAttribute("userRole", user.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));
        model.addAttribute("allRoles", roleService.getRoles());
        model.addAttribute("userForm", userForm);
        return "user";
    }

    @GetMapping("/user/{id}")
    public String getUser(@PathVariable int id, Model model) {
        var user = userService.findById((long) id);
        if (user.isEmpty()) {
            model.addAttribute("reason", "Не найден пользователь с id " + id);
            return "error";
        } else {
            model.addAttribute("userName", user.get().getUsername());
            model.addAttribute("me", userService.getUser() == user.get());
            return "other_user";
        }
    }

    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userService.getUserList());
        return "users";
    }
}
