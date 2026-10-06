package com.example.moattravel.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.moattravel.entity.User;
import com.example.moattravel.event.SignupEventPublisher;
import com.example.moattravel.form.SignupForm;
import com.example.moattravel.service.UserService;

@Controller
public class AuthController {
    private final UserService userService;
    private final SignupEventPublisher signupEventPublisher;

    public AuthController(UserService userService, SignupEventPublisher signupEventPublisher) {
        this.userService = userService;
        this.signupEventPublisher = signupEventPublisher;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute @Validated SignupForm signupForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         HttpServletRequest httpServletRequest)
    {
        // メールアドレスが登録済みかどうかをチェック
        if (userService.isEmailRegistered(signupForm.getEmail())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録されているメールアドレスです。");
            bindingResult.addError(fieldError);
        }

        // パスワードとパスワード（確認用）の入力値が一致するかどうかをチェック
        if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
            bindingResult.addError(fieldError);
        }

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        User createdUser = userService.create(signupForm);
        String requestUrl = new String(httpServletRequest.getRequestURL());
        signupEventPublisher.publishSignupEvent(createdUser, requestUrl);
        redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了させてください。");

        return "redirect:/";
    }
}