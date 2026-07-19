package com.miniproject.demo.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.common.excel.ExcelWriter;
import com.miniproject.demo.domain.DemoItem;
import com.miniproject.demo.domain.DemoItemRepository;
import com.miniproject.demo.dto.DemoItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DemoItemService {

    private static final int EXPORT_MAX_ROWS = 5000;
    private static final List<String> EXPORT_HEADERS = List.of(
            "ID", "상품명", "카테고리", "상태", "가격", "재고", "담당자", "추천", "수정일", "설명"
    );

    private final DemoItemRepository demoItemRepository;

    public DemoItemService(DemoItemRepository demoItemRepository) {
        this.demoItemRepository = demoItemRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> search(
            String keyword,
            String category,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean featured,
            Boolean inStock,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;

        String kw = blankToNull(keyword);
        String cat = blankToNull(category);
        String st = blankToNull(status);

        List<DemoItemResponse> items = demoItemRepository.findPage(
                kw, cat, st, dateFrom, dateTo, featured, inStock, safeSize, offset
        ).stream().map(DemoItemResponse::from).toList();
        long total = demoItemRepository.countPage(kw, cat, st, dateFrom, dateTo, featured, inStock);

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("page", safePage);
        result.put("size", safeSize);
        result.put("total", total);
        return result;
    }

    @Transactional(readOnly = true)
    public DemoItemResponse getById(Long id) {
        return demoItemRepository.findById(id)
                .map(DemoItemResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "데모 상품을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public byte[] exportExcel(
            String keyword,
            String category,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean featured,
            Boolean inStock
    ) {
        String kw = blankToNull(keyword);
        String cat = blankToNull(category);
        String st = blankToNull(status);
        List<DemoItem> items = demoItemRepository.findForExport(
                kw, cat, st, dateFrom, dateTo, featured, inStock, EXPORT_MAX_ROWS
        );
        List<List<Object>> rows = items.stream()
                .map(item -> {
                    List<Object> row = new java.util.ArrayList<>(10);
                    row.add(item.getId() != null ? item.getId() : 0);
                    row.add(nullToEmpty(item.getName()));
                    row.add(nullToEmpty(item.getCategory()));
                    row.add(statusLabel(item.getStatus()));
                    row.add(item.getPrice() != null ? item.getPrice() : 0);
                    row.add(item.getStock() != null ? item.getStock() : 0);
                    row.add(nullToEmpty(item.getOwner()));
                    row.add(item.isFeatured() ? "Y" : "N");
                    row.add(item.getUpdatedAt() != null ? item.getUpdatedAt() : "");
                    row.add(nullToEmpty(item.getDescription()));
                    return row;
                })
                .toList();
        return ExcelWriter.write("상품목록", EXPORT_HEADERS, rows);
    }

    private static String statusLabel(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "ACTIVE" -> "판매중";
            case "INACTIVE" -> "판매중지";
            case "PENDING" -> "검수중";
            default -> status;
        };
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
