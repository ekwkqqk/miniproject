package com.miniproject.demo.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
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

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
