package com.miniproject.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MenuAccessRequest {

    @NotBlank(message = "메뉴 URL을 입력해주세요.")
    @Size(max = 200)
    private String menuUrl;

    public String getMenuUrl() {
        return menuUrl;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }
}
