package com.example.moattravel.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.moattravel.entity.House;
import com.example.moattravel.entity.Reservation;
import com.example.moattravel.entity.User;
import com.example.moattravel.form.ReservationInputForm;
import com.example.moattravel.form.ReservationRegisterForm;
import com.example.moattravel.repository.HouseRepository;
import com.example.moattravel.repository.ReservationRepository;
import com.example.moattravel.security.UserDetailsImpl;
import com.example.moattravel.service.ReservationService;
import com.example.moattravel.service.StripeService;

@Controller
public class ReservationController {
    private final ReservationRepository reservationRepository;
    private final HouseRepository houseRepository;
    private final ReservationService reservationService;
    private final StripeService stripeService;

    public ReservationController(ReservationRepository reservationRepository, 
                                 HouseRepository houseRepository, 
                                 ReservationService reservationService, 
                                 StripeService stripeService) {
        this.reservationRepository = reservationRepository;
        this.houseRepository = houseRepository;
        this.reservationService = reservationService;
        this.stripeService = stripeService;
    }

 // 予約一覧ページ表示（決済完了時のデータ保存含む）
    @GetMapping("/reservations")
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, 
                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, 
                        Model model, 
                        HttpServletRequest httpServletRequest) {
        User user = userDetailsImpl.getUser();
        
       
        String sessionId = httpServletRequest.getParameter("session_id");
        if (sessionId != null) {
            stripeService.createReservation(sessionId);
            model.addAttribute("successMessage", "民宿の予約が完了しました。");
        }
        
        Page<Reservation> reservationPage = reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        model.addAttribute("reservationPage", reservationPage);
        
        return "reservations/index";
    }

    // 詳細画面からの入力確認処理
    @GetMapping("/houses/{id}/reservations/input")
    public String input(@PathVariable(name = "id") Integer id,
                        @ModelAttribute @Validated ReservationInputForm reservationInputForm,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        House house = houseRepository.getReferenceById(id);
        Integer capacity = house.getCapacity();
        
        if (reservationInputForm.getNumberOfPeople() != null && reservationInputForm.getNumberOfPeople() > capacity) {
            bindingResult.rejectValue("numberOfPeople", "value.cannotBeGreaterThanCapacity", "宿泊人数が定員を超えています。");
        }
        
        // バリデーションエラー時は詳細画面へ戻る
        if (bindingResult.hasErrors()) {
            model.addAttribute("house", house);
            model.addAttribute("reservationInputForm", reservationInputForm);
            return "houses/show";
        }
        
        redirectAttributes.addFlashAttribute("reservationInputForm", reservationInputForm);
        
        return "redirect:/houses/{id}/reservations/confirm";
    }

    // 予約内容確認画面の表示
    @GetMapping("/houses/{id}/reservations/confirm")
    public String confirm(@PathVariable(name = "id") Integer id,
                          @ModelAttribute ReservationInputForm reservationInputForm,
                          @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        String checkinDateOut = reservationInputForm.getFromCheckinDateToCheckoutDate();
        
        // ガード処理（日付未選択等の防止）
        if (checkinDateOut == null || !checkinDateOut.contains(" から ")) {
            redirectAttributes.addFlashAttribute("errorMessage", "宿泊期間を正しく選択してください。");
            return "redirect:/houses/{id}";
        }

        String[] checkinDateOutArray = checkinDateOut.split(" から ");
        String checkinDate = checkinDateOutArray[0];
        String checkoutDate = checkinDateOutArray[1];

        House house = houseRepository.getReferenceById(id);
        User user = userDetailsImpl.getUser();
        Integer price = house.getPrice();
        Integer amount = reservationService.calculateAmount(checkinDate, checkoutDate, price);

        ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm(
                house.getId(), 
                user.getId(), 
                checkinDate, 
                checkoutDate, 
                reservationInputForm.getNumberOfPeople(), 
                amount
        );

        model.addAttribute("house", house);
        model.addAttribute("reservationRegisterForm", reservationRegisterForm);

        return "reservations/confirm";
    }

    // Stripe決済セッションの生成とリダイレクト処理
    @PostMapping("/houses/{id}/reservations/create")
    public String create(@PathVariable(name = "id") Integer id,
                         @ModelAttribute ReservationRegisterForm reservationRegisterForm,
                         HttpServletRequest httpServletRequest) {
        House house = houseRepository.getReferenceById(id);
        
        String redirectUrl = stripeService.createStripeSession(house.getName(), reservationRegisterForm, httpServletRequest);
        
        return "redirect:" + redirectUrl;
    }
}