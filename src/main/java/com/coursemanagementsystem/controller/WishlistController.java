package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.model.Wishlist;
import com.coursemanagementsystem.service.UserService;
import com.coursemanagementsystem.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    @PostMapping("/wishlist/toggle/{courseId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWishlist(@PathVariable Long courseId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thực hiện tính năng này");
            return ResponseEntity.status(401).body(response);
        }

        User user = userService.findByUsername(principal.getName());
        boolean isAdded = wishlistService.toggleWishlist(user, courseId);
        
        response.put("success", true);
        response.put("isAdded", isAdded);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/wishlist")
    public String viewWishlist(Model model, Principal principal,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "6") int size) {
        if (principal == null) return "redirect:/auth/login";

        User user = userService.findByUsername(principal.getName());
        Page<Wishlist> wishlistPage = wishlistService.getWishlistByUser(user, PageRequest.of(page, size));
        
        model.addAttribute("user", user);
        model.addAttribute("wishlistPage", wishlistPage);
        model.addAttribute("activeTab", "wishlist");
        return "profile/wishlist";
    }
}
