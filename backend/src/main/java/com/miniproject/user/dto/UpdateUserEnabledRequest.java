package com.miniproject.user.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateUserEnabledRequest {

    @NotNull(message = "활성 여부를 입력해주세요.")
    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
