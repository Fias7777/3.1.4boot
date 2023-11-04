package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }


    @GetMapping
    public String pageForAdmin(Model model, Principal principal) {
        List<User> userList = userService.findAll();
        User user = userList
                .stream()
                .filter(user1 -> user1.getLogin().equals(principal.getName()))
                .findFirst().orElseThrow(RuntimeException::new);

        model.addAttribute("newUser", new User());
        model.addAttribute("user", user);
        model.addAttribute("roles", user.getStringRoles());
        model.addAttribute("users", userList);
        return "admin";
    }

    @GetMapping("/create")
    public String templateForCreateUser(ModelMap model) {
        model.addAttribute("newUser", new User());
        List<Role> availableRoles = roleService.getAllRoles();
        model.addAttribute("availableRoles", availableRoles);
        return "admin";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("newUser") User user, BindingResult bindingResult,
                             @RequestParam("listRoles") List<Long> roles,
                             ModelMap modelMap) {
        if (bindingResult.hasErrors()) {
            final List<String> strings = userService.parseFieldsErrors(bindingResult.getFieldErrors());
            modelMap.addAttribute("errorsList", strings);
            return "errors";
        }
        List<Role> rolesList = roleService.findByIdRoles(roles);
        user.setRoles(rolesList);
        userService.create(user);
        return "redirect:/admin";
    }


    @GetMapping("/edit/{id}")
    public String TemplateForUpdatingUser(ModelMap model, @PathVariable("id") Long id) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAllRoles());
        user.setPassword(null);
        return "admin";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public String updateUser(@Valid User user,
                             BindingResult bindingResult,
                             @RequestParam("rolesList") List<Long> roles,
                             ModelMap modelMap
    ) {
        if (bindingResult.hasErrors()) {
            final List<String> strings = userService.parseFieldsErrors(bindingResult.getFieldErrors());
            modelMap.addAttribute("errorsList", strings);
            return "errors";
        }
        user.setRoles(roleService.findByIdRoles(roles));
        userService.update(user);
        return "redirect:/admin";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteById(id);
        return "redirect:/admin";
    }

}

