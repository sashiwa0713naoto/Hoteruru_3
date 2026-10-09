package com.example.moattravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.moattravel.entity.User;
import com.example.moattravel.form.UserEditForm;
import com.example.moattravel.repository.ReservationRepository;
import com.example.moattravel.repository.UserRepository;
import com.example.moattravel.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final UserService userService;

    public AdminUserController(UserRepository userRepository, 
                               ReservationRepository reservationRepository, 
                               UserService userService) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.userService = userService;
    }

    // 会員一覧
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

    // 会員詳細
    @GetMapping("/{id}")
    public String show(@PathVariable(name = "id") Integer id, Model model) {
        User user = userRepository.getReferenceById(id);
        model.addAttribute("user", user);
        return "admin/users/show";
    }

    // 会員編集画面表示
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable(name = "id") Integer id, Model model) {
        User user = userRepository.getReferenceById(id);
        UserEditForm userEditForm = new UserEditForm(
            user.getId(), 
            user.getName(), 
            user.getFurigana(), 
            user.getPostalCode(), 
            user.getAddress(), 
            user.getPhoneNumber(), 
            user.getEmail()
        );
        
        model.addAttribute("userEditForm", userEditForm);
        model.addAttribute("user", user);
        
        return "admin/users/edit";
    }

    // 会員情報更新
    @PostMapping("/{id}/update")
    public String update(@PathVariable(name = "id") Integer id,
                         @ModelAttribute @Validated UserEditForm userEditForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            User user = userRepository.getReferenceById(id);
            model.addAttribute("user", user);
            return "admin/users/edit";
        }
        
        userService.update(userEditForm);
        redirectAttributes.addFlashAttribute("successMessage", "会員情報を更新しました。");
        
        return "redirect:/admin/users/{id}";
    }

    // 会員削除
    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
        User user = userRepository.getReferenceById(id);
        
        // 予約データの紐づき解除・削除
        reservationRepository.deleteByUser(user);
        
        // 会員削除
        userRepository.delete(user);
        
        redirectAttributes.addFlashAttribute("successMessage", "会員を削除しました。");
        
        return "redirect:/admin/users";
    }
}