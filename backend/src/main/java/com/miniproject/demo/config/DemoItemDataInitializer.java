package com.miniproject.demo.config;

import com.miniproject.demo.domain.DemoItem;
import com.miniproject.demo.domain.DemoItemRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DemoItemDataInitializer {

    private final DemoItemRepository demoItemRepository;

    public DemoItemDataInitializer(DemoItemRepository demoItemRepository) {
        this.demoItemRepository = demoItemRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (demoItemRepository.countAll() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<SeedRow> rows = List.of(
                row("노트북 Pro 14", "전자제품", "ACTIVE", 1890000, 24, "김민수", "가벼운 업무용 노트북.", true, "2026-07-01"),
                row("무선 키보드", "주변기기", "ACTIVE", 89000, 120, "이서연", "저소음 멤브레인 키보드.", false, "2026-07-03"),
                row("모니터 27인치", "전자제품", "INACTIVE", 320000, 8, "박준호", "QHD IPS 패널 모니터.", true, "2026-06-28"),
                row("USB-C 허브", "주변기기", "ACTIVE", 45000, 56, "최유진", "HDMI / SD / USB3 멀티 허브.", false, "2026-07-05"),
                row("스탠딩 데스크", "가구", "ACTIVE", 450000, 15, "정하늘", "높이 조절형 스탠딩 데스크.", true, "2026-07-08"),
                row("인체공학 의자", "가구", "PENDING", 280000, 3, "오세린", "요추 지지 메쉬 의자.", false, "2026-07-09"),
                row("무선 마우스", "주변기기", "ACTIVE", 39000, 200, "김민수", "저지연 무선 마우스.", false, "2026-07-10"),
                row("웹캠 HD", "전자제품", "ACTIVE", 79000, 0, "이서연", "재고 없음 샘플.", false, "2026-07-02"),
                row("노트북 스탠드", "가구", "ACTIVE", 35000, 40, "박준호", "각도 조절 알루미늄 스탠드.", true, "2026-06-20"),
                row("기계식 키보드", "주변기기", "PENDING", 159000, 12, "최유진", "청축 텐키리스.", false, "2026-07-11"),
                row("태블릿 11인치", "전자제품", "ACTIVE", 650000, 18, "정하늘", "드로잉용 태블릿.", true, "2026-07-12"),
                row("외장 SSD 1TB", "주변기기", "ACTIVE", 129000, 0, "오세린", "품절 상태 샘플.", false, "2026-06-15"),
                row("책상 선반", "가구", "INACTIVE", 62000, 7, "김민수", "모니터 상단 선반.", false, "2026-05-30"),
                row("헤드셋", "전자제품", "ACTIVE", 98000, 33, "이서연", "노이즈 캔슬링 헤드셋.", true, "2026-07-13"),
                row("케이블 정리함", "주변기기", "ACTIVE", 18000, 90, "박준호", "책상 하단 정리함.", false, "2026-07-04"),
                row("회의용 스피커", "전자제품", "PENDING", 210000, 5, "최유진", "블루투스 스피커폰.", false, "2026-07-14"),
                row("파일 캐비닛", "가구", "ACTIVE", 175000, 0, "정하늘", "3단 캐비닛 (품절).", false, "2026-06-10"),
                row("도킹 스테이션", "주변기기", "ACTIVE", 245000, 22, "오세린", "듀얼 모니터 도킹.", true, "2026-07-15"),
                row("스마트 조명", "전자제품", "INACTIVE", 52000, 11, "김민수", "책상 조명.", false, "2026-05-18"),
                row("발받침", "가구", "ACTIVE", 29000, 48, "이서연", "각도 조절 발받침.", false, "2026-07-06"),
                row("휴대용 모니터", "전자제품", "ACTIVE", 289000, 9, "박준호", "15.6인치 FHD.", true, "2026-07-16"),
                row("USB 메모리 128GB", "주변기기", "ACTIVE", 22000, 300, "최유진", "대량 재고 샘플.", false, "2026-07-07"),
                row("화이트보드", "가구", "PENDING", 88000, 4, "정하늘", "자석형 화이트보드.", false, "2026-06-25"),
                row("마이크", "전자제품", "ACTIVE", 115000, 14, "오세린", "USB 콘덴서 마이크.", true, "2026-07-17"),
                row("마우스 패드 XL", "주변기기", "INACTIVE", 25000, 0, "김민수", "단종/품절 샘플.", false, "2026-04-12")
        );

        for (SeedRow row : rows) {
            DemoItem item = new DemoItem();
            item.setName(row.name());
            item.setCategory(row.category());
            item.setStatus(row.status());
            item.setPrice(row.price());
            item.setStock(row.stock());
            item.setOwner(row.owner());
            item.setDescription(row.description());
            item.setFeatured(row.featured());
            item.setUpdatedAt(LocalDate.parse(row.updatedAt()));
            item.setCreatedAt(now);
            demoItemRepository.insert(item);
        }
    }

    private static SeedRow row(
            String name,
            String category,
            String status,
            int price,
            int stock,
            String owner,
            String description,
            boolean featured,
            String updatedAt
    ) {
        return new SeedRow(name, category, status, price, stock, owner, description, featured, updatedAt);
    }

    private record SeedRow(
            String name,
            String category,
            String status,
            int price,
            int stock,
            String owner,
            String description,
            boolean featured,
            String updatedAt
    ) {
    }
}
