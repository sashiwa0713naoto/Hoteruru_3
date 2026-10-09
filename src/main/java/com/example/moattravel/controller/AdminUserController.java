package com.example.moattravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.moattravel.entity.User;
import com.example.moattravel.repository.UserRepository;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    
    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 会員一覧画面
    @GetMapping
    public String index(@RequestParam(name = "keyword", required = false) String keyword,
                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                        Model model) {
        Page<User> userPage;

        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findByNameLikeOrFuriganaLike("%" + keyword + "%", "%" + keyword + "%", pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        model.addAttribute("userPage", userPage);
        model.addAttribute("keyword", keyword);

        return "admin/users/index";
    }

    // 会員詳細画面
    @GetMapping("/{id}")
    public String show(@PathVariable(name = "id") Integer id, Model model) {
        User user = userRepository.getReferenceById(id);
        model.addAttribute("user", user);

        return "admin/users/show";
    }

    // 会員削除処理
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "会員を削除しました。");

        return "redirect:/admin/users";
    }
}