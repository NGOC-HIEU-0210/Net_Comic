package com.hieu.enums;



public enum ImportStatus {
    PENDING,      // vừa tạo, đang chờ xử lý
    PROCESSING,   // đang giải nén, nén ảnh, lưu ảnh
    DONE,         // xong, các trang đã được lưu
    FAILED        // lỗi (file hỏng, ảnh sai định dạng...)
}
