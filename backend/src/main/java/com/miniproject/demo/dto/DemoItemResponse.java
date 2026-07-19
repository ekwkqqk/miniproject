package com.miniproject.demo.dto;

import com.miniproject.demo.domain.DemoItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DemoItemResponse(
        Long id,
        String name,
        String category,
        String status,
        Integer price,
        Integer stock,
        String owner,
        String description,
        boolean featured,
        LocalDate updatedAt,
        LocalDateTime createdAt
) {
    public static DemoItemResponse from(DemoItem item) {
        return new DemoItemResponse(
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getStatus(),
                item.getPrice(),
                item.getStock(),
                item.getOwner(),
                item.getDescription(),
                item.isFeatured(),
                item.getUpdatedAt(),
                item.getCreatedAt()
        );
    }
}
