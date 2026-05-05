package com.coursemanagementsystem.controller.admin;

import com.coursemanagementsystem.model.Category;
import com.coursemanagementsystem.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Hiển thị danh sách danh mục (dạng table với pagination)
     */
    @GetMapping("")
    public String categoryList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            Model model) {

        List<Category> allCategories = categoryService.getAllCategories();

        // Lọc theo keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            allCategories = allCategories.stream()
                    .filter(cat -> cat.getName().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
        }

        // Tính toán pagination
        int totalCategories = allCategories.size();
        int normalizedPage = Math.max(page - 1, 0);
        int totalPages = (int) Math.ceil((double) totalCategories / size);

        int start = normalizedPage * size;
        int end = Math.min(start + size, totalCategories);

        List<Category> paginatedCategories = start >= totalCategories ? List.of() : allCategories.subList(start, end);

        model.addAttribute("categories", paginatedCategories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalCategories);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "categories");

        return "admin/category/category-list";
    }

    /**
     * Form tạo danh mục mới
     */
    @GetMapping("/create")
    public String createCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("activeMenu", "categories");
        return "admin/category/create-category";
    }

    /**
     * Lưu danh mục mới
     */
    @PostMapping("/save")
    public String saveCategory(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "categories");
            return "admin/category/create-category";
        }

        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Danh mục đã được tạo thành công!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi khi tạo danh mục: " + e.getMessage());
            model.addAttribute("activeMenu", "categories");
            return "admin/category/create-category";
        }
    }

    /**
     * Hiển thị chi tiết danh mục
     */
    @GetMapping("/{id}")
    public String categoryDetail(
            @PathVariable("id") Long id,
            Model model) {

        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            return "redirect:/admin/categories";
        }

        model.addAttribute("category", category);
        model.addAttribute("activeMenu", "categories");
        return "admin/category/category-detail";
    }

    @PostMapping("/{id}/update")
    public String updateCategory(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Category originalCategory = categoryService.getCategoryById(id);
            model.addAttribute("category", category);
            model.addAttribute("activeMenu", "categories");
            return "admin/category/category-detail";
        }

        try {
            category.setId(id);
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/categories/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Danh mục đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi khi xóa danh mục: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }
}