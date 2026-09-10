package com.ats.admin;

import com.ats.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ApiResponse<List<AdminUserResponse>> users() {
        return ApiResponse.ok("Users loaded", adminService.users());
    }

    @GetMapping("/users/{id}")
    public ApiResponse<AdminUserResponse> user(@PathVariable Long id) {
        return ApiResponse.ok("User loaded", adminService.user(id));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<AdminUserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        return ApiResponse.ok("User updated", adminService.update(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return ApiResponse.ok("User deleted", null);
    }

    @GetMapping("/jobs")
    public ApiResponse<List<AdminJobResponse>> jobs() {
        return ApiResponse.ok("Jobs loaded", adminService.jobs());
    }

    @GetMapping("/applications")
    public ApiResponse<List<AdminApplicationResponse>> applications() {
        return ApiResponse.ok("Applications loaded", adminService.applications());
    }

    @GetMapping("/reports")
    public ApiResponse<AdminReportResponse> reports() {
        return ApiResponse.ok("Reports loaded", adminService.reports());
    }

    @GetMapping("/notifications")
    public ApiResponse<List<AdminNotificationResponse>> notifications() {
        return ApiResponse.ok("Notifications loaded", adminService.notifications());
    }
}
