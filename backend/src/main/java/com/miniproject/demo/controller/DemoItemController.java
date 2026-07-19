package com.miniproject.demo.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.common.excel.ExcelResponses;
import com.miniproject.demo.dto.DemoItemResponse;
import com.miniproject.demo.service.DemoItemService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/items")
public class DemoItemController {

    private final DemoItemService demoItemService;

    public DemoItemController(DemoItemService demoItemService) {
        this.demoItemService = demoItemService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(demoItemService.search(
                keyword, category, status, dateFrom, dateTo, featured, inStock, page, size
        ));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean inStock
    ) {
        byte[] body = demoItemService.exportExcel(
                keyword, category, status, dateFrom, dateTo, featured, inStock
        );
        return ExcelResponses.xlsx(body, "demo-items");
    }

    @GetMapping("/{id}")
    public ApiResponse<DemoItemResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(demoItemService.getById(id));
    }
}
