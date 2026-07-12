package com.miniproject.menu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MenuReorderRequest {

    @NotNull(message = "메뉴 ID가 필요합니다.")
    private Long menuId;

    /** null이면 최상위 */
    private Long parentId;

    /** 같은 부모 아래 정렬된 메뉴 ID 목록 (드래그 대상 포함) */
    @NotEmpty(message = "정렬할 메뉴 목록이 필요합니다.")
    private List<Long> orderedSiblingIds = new ArrayList<>();

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<Long> getOrderedSiblingIds() {
        return orderedSiblingIds;
    }

    public void setOrderedSiblingIds(List<Long> orderedSiblingIds) {
        this.orderedSiblingIds = orderedSiblingIds != null ? orderedSiblingIds : new ArrayList<>();
    }
}
