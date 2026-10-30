package com.hieu.controller;

import com.hieu.payload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "Thông tin cơ bản của các dịch vụ")
@RestController
public class HomeController {
    @Operation(summary = "Kiểm tra tình trạng dịch vụ", description = "Trả về thông tin cơ bản của Catalog Service.")
    @GetMapping()
    public ApiResponse HomeController() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Đây là Catalog Service");
        return apiResponse;
    }

}
