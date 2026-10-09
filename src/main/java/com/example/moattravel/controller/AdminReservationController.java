package com.example.moattravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.moattravel.entity.Reservation;
import com.example.moattravel.repository.ReservationRepository;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationRepository reservationRepository;

    public AdminReservationController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }
    @GetMapping
    public String index(@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.DESC) Pageable pageable, Model model) {
        Page<Reservation> reservationPage = reservationRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        model.addAttribute("reservationPage", reservationPage);
        
        return "admin/reservations/index";
    }
}