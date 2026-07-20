package com.miniproject.approval.service;

import com.miniproject.approval.domain.ApprovalDocument;
import com.miniproject.approval.domain.ApprovalDocumentRepository;
import com.miniproject.approval.domain.ApprovalHistory;
import com.miniproject.approval.domain.ApprovalHistoryRepository;
import com.miniproject.approval.domain.ApprovalLine;
import com.miniproject.approval.domain.ApprovalLineRepository;
import com.miniproject.approval.domain.ApprovalStatuses;
import com.miniproject.approval.dto.ApprovalActionRequest;
import com.miniproject.approval.dto.ApprovalDocumentRequest;
import com.miniproject.approval.dto.ApprovalDocumentResponse;
import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.user.domain.User;
import com.miniproject.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class ApprovalService {

    private final ApprovalDocumentRepository documentRepository;
    private final ApprovalLineRepository lineRepository;
    private final ApprovalHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public ApprovalService(ApprovalDocumentRepository documentRepository,
                           ApprovalLineRepository lineRepository,
                           ApprovalHistoryRepository historyRepository,
                           UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.lineRepository = lineRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> search(String email, String box, String status, String keyword, int page, int size) {
        User me = requireUser(email);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;
        String kw = blankToNull(keyword);
        String st = blankToNull(status);
        String bx = box == null || box.isBlank() ? "related" : box.trim();

        List<ApprovalDocument> docs = documentRepository.findPage(me.getId(), bx, st, kw, safeSize, offset);
        long total = documentRepository.countPage(me.getId(), bx, st, kw);
        List<Map<String, Object>> items = docs.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("docNo", d.getDocNo());
            m.put("title", d.getTitle());
            m.put("status", d.getStatus());
            m.put("currentStep", d.getCurrentStep());
            m.put("drafterId", d.getDrafterId());
            m.put("submittedAt", d.getSubmittedAt());
            m.put("completedAt", d.getCompletedAt());
            m.put("updatedAt", d.getUpdatedAt());
            m.put("createdAt", d.getCreatedAt());
            return m;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("page", safePage);
        result.put("size", safeSize);
        result.put("total", total);
        return result;
    }

    @Transactional(readOnly = true)
    public ApprovalDocumentResponse get(String email, Long id) {
        User me = requireUser(email);
        ApprovalDocument doc = requireDoc(id);
        assertCanView(me, doc);
        return toResponse(doc);
    }

    @Transactional
    public ApprovalDocumentResponse create(String email, ApprovalDocumentRequest request) {
        User me = requireUser(email);
        validateLines(me.getId(), request.getLines(), false);
        LocalDateTime now = LocalDateTime.now();
        ApprovalDocument doc = new ApprovalDocument();
        doc.setDocNo(nextDocNo());
        doc.setTitle(request.getTitle().trim());
        doc.setContent(request.getContent().trim());
        doc.setStatus(ApprovalStatuses.DRAFT);
        doc.setCurrentStep(null);
        doc.setVersion(0);
        doc.setDrafterId(me.getId());
        doc.setFileGroupId(blankToNull(request.getFileGroupId()));
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);
        documentRepository.insert(doc);
        replaceLines(doc.getId(), request.getLines(), true);
        addHistory(doc.getId(), me.getId(), "CREATE", null, null, null);
        return toResponse(requireDoc(doc.getId()));
    }

    @Transactional
    public ApprovalDocumentResponse update(String email, Long id, ApprovalDocumentRequest request) {
        User me = requireUser(email);
        ApprovalDocument doc = requireDoc(id);
        assertDrafter(me, doc);
        if (!ApprovalStatuses.DRAFT.equals(doc.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "임시저장 문서만 수정할 수 있습니다.");
        }
        validateLines(me.getId(), request.getLines(), false);
        doc.setTitle(request.getTitle().trim());
        doc.setContent(request.getContent().trim());
        doc.setFileGroupId(blankToNull(request.getFileGroupId()));
        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdatedAt(LocalDateTime.now());
        if (documentRepository.update(doc) == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "다른 사용자가 문서를 수정했습니다. 새로고침 후 다시 시도하세요.");
        }
        replaceLines(doc.getId(), request.getLines(), true);
        addHistory(doc.getId(), me.getId(), "UPDATE", null, null, null);
        return toResponse(requireDoc(doc.getId()));
    }

    @Transactional
    public void delete(String email, Long id) {
        User me = requireUser(email);
        ApprovalDocument doc = requireDoc(id);
        assertDrafter(me, doc);
        if (!ApprovalStatuses.DRAFT.equals(doc.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "임시저장 문서만 삭제할 수 있습니다.");
        }
        documentRepository.deleteById(id);
    }

    @Transactional
    public ApprovalDocumentResponse submit(String email, Long id) {
        User me = requireUser(email);
        ApprovalDocument doc = requireDoc(id);
        assertDrafter(me, doc);
        if (!ApprovalStatuses.DRAFT.equals(doc.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "임시저장 문서만 상신할 수 있습니다.");
        }
        if (doc.getTitle() == null || doc.getTitle().isBlank() || doc.getContent() == null || doc.getContent().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "제목과 내용은 필수입니다.");
        }
        List<ApprovalLine> lines = lineRepository.findByDocumentId(id);
        validatePersistedLines(me.getId(), lines, true);

        for (ApprovalLine line : lines) {
            if (ApprovalStatuses.isBlocking(line.getLineType())) {
                line.setStatus(ApprovalStatuses.LINE_WAITING);
                line.setActive(false);
            } else {
                line.setStatus(ApprovalStatuses.LINE_PENDING);
                line.setActive(false);
            }
            lineRepository.update(line);
        }

        Integer minStep = lines.stream()
                .filter(l -> ApprovalStatuses.isBlocking(l.getLineType()))
                .map(ApprovalLine::getStepOrder)
                .min(Integer::compareTo)
                .orElse(null);
        if (minStep == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "결재 또는 합의 라인이 최소 1개 필요합니다.");
        }
        activateStep(lines, minStep);

        LocalDateTime now = LocalDateTime.now();
        doc.setStatus(ApprovalStatuses.IN_PROGRESS);
        doc.setCurrentStep(minStep);
        doc.setSubmittedAt(now);
        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdatedAt(now);
        if (documentRepository.update(doc) == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "문서 상태 충돌입니다. 새로고침 후 다시 시도하세요.");
        }
        addHistory(id, me.getId(), "SUBMIT", minStep, null, null);
        return toResponse(requireDoc(id));
    }

    @Transactional
    public ApprovalDocumentResponse recall(String email, Long id) {
        User me = requireUser(email);
        ApprovalDocument doc = requireDoc(id);
        assertDrafter(me, doc);
        if (!ApprovalStatuses.IN_PROGRESS.equals(doc.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "진행 중 문서만 회수할 수 있습니다.");
        }
        if (lineRepository.countActedByDocumentId(id) > 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 처리된 결재가 있어 회수할 수 없습니다.");
        }
        List<ApprovalLine> lines = lineRepository.findByDocumentId(id);
        for (ApprovalLine line : lines) {
            line.setStatus(ApprovalStatuses.LINE_WAITING);
            line.setActive(false);
            line.setActedAt(null);
            line.setComment(null);
            lineRepository.update(line);
        }
        doc.setStatus(ApprovalStatuses.DRAFT);
        doc.setCurrentStep(null);
        doc.setSubmittedAt(null);
        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdatedAt(LocalDateTime.now());
        documentRepository.update(doc);
        addHistory(id, me.getId(), "RECALL", null, null, null);
        return toResponse(requireDoc(id));
    }

    @Transactional
    public ApprovalDocumentResponse approve(String email, Long documentId, ApprovalActionRequest request) {
        return actApproveOrReject(email, documentId, request, true);
    }

    @Transactional
    public ApprovalDocumentResponse reject(String email, Long documentId, ApprovalActionRequest request) {
        if (request.getComment() == null || request.getComment().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "반려 사유는 필수입니다.");
        }
        return actApproveOrReject(email, documentId, request, false);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> badges(String email) {
        User me = requireUser(email);
        Map<String, Long> result = new HashMap<>();
        result.put("inbox", documentRepository.countPage(me.getId(), "inbox", null, null));
        result.put("notices", documentRepository.countPage(me.getId(), "notices", "PENDING", null));
        return result;
    }

    @Transactional
    public ApprovalDocumentResponse acknowledge(String email, Long documentId, ApprovalActionRequest request) {
        User me = requireUser(email);
        ApprovalLine line = lineRepository.findById(request.getLineId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결재선을 찾을 수 없습니다."));
        if (!Objects.equals(line.getDocumentId(), documentId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "문서와 결재선이 일치하지 않습니다.");
        }
        ApprovalDocument doc = requireDoc(line.getDocumentId());
        assertCanView(me, doc);
        if (!ApprovalStatuses.TYPE_NOTIFY.equals(line.getLineType())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "통보 라인만 확인할 수 있습니다.");
        }
        if (!Objects.equals(line.getApproverId(), me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 통보만 확인할 수 있습니다.");
        }
        if (!ApprovalStatuses.LINE_PENDING.equals(line.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 처리된 통보입니다.");
        }
        line.setStatus(ApprovalStatuses.LINE_ACKNOWLEDGED);
        line.setActedAt(LocalDateTime.now());
        line.setComment(blankToNull(request.getComment()));
        lineRepository.update(line);
        addHistory(doc.getId(), me.getId(), "ACK", line.getStepOrder(), line.getId(), line.getComment());
        return toResponse(requireDoc(doc.getId()));
    }

    private ApprovalDocumentResponse actApproveOrReject(String email, Long documentId, ApprovalActionRequest request, boolean approve) {
        User me = requireUser(email);
        ApprovalLine line = lineRepository.findById(request.getLineId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결재선을 찾을 수 없습니다."));
        if (!Objects.equals(line.getDocumentId(), documentId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "문서와 결재선이 일치하지 않습니다.");
        }
        ApprovalDocument doc = requireDoc(line.getDocumentId());
        if (!Objects.equals(line.getApproverId(), me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 결재선만 처리할 수 있습니다.");
        }
        if (ApprovalStatuses.TYPE_NOTIFY.equals(line.getLineType())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "통보는 승인/반려할 수 없습니다.");
        }
        if (!ApprovalStatuses.LINE_PENDING.equals(line.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "대기 상태가 아닌 라인입니다.");
        }
        if (ApprovalStatuses.isBlocking(line.getLineType()) && !line.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "아직 처리 순서가 아닙니다.");
        }
        if (!ApprovalStatuses.IN_PROGRESS.equals(doc.getStatus())
                && !(ApprovalStatuses.APPROVED.equals(doc.getStatus()) && ApprovalStatuses.TYPE_POST.equals(line.getLineType()))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "처리할 수 없는 문서 상태입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        line.setStatus(approve ? ApprovalStatuses.LINE_APPROVED : ApprovalStatuses.LINE_REJECTED);
        line.setActive(false);
        line.setActedAt(now);
        line.setComment(blankToNull(request.getComment()));
        lineRepository.update(line);
        addHistory(doc.getId(), me.getId(), approve ? "APPROVE" : "REJECT", line.getStepOrder(), line.getId(), line.getComment());

        List<ApprovalLine> lines = lineRepository.findByDocumentId(doc.getId());
        if (!approve) {
            rejectDocument(doc, lines, now);
        } else if (ApprovalStatuses.isBlocking(line.getLineType())) {
            advanceAfterBlockingApprove(doc, lines, line.getStepOrder(), now);
        } else {
            // POST approve only
            doc.setVersion(doc.getVersion() + 1);
            doc.setUpdatedAt(now);
            documentRepository.update(doc);
        }
        return toResponse(requireDoc(doc.getId()));
    }

    private void rejectDocument(ApprovalDocument doc, List<ApprovalLine> lines, LocalDateTime now) {
        for (ApprovalLine l : lines) {
            if (ApprovalStatuses.LINE_PENDING.equals(l.getStatus()) || ApprovalStatuses.LINE_WAITING.equals(l.getStatus())) {
                l.setStatus(ApprovalStatuses.LINE_SKIPPED);
                l.setActive(false);
                lineRepository.update(l);
            }
        }
        doc.setStatus(ApprovalStatuses.REJECTED);
        doc.setCompletedAt(now);
        doc.setCurrentStep(null);
        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdatedAt(now);
        documentRepository.update(doc);
    }

    private void advanceAfterBlockingApprove(ApprovalDocument doc, List<ApprovalLine> lines, int step, LocalDateTime now) {
        boolean stepDone = lines.stream()
                .filter(l -> l.getStepOrder() == step && ApprovalStatuses.isBlocking(l.getLineType()))
                .allMatch(l -> ApprovalStatuses.LINE_APPROVED.equals(l.getStatus()));
        if (!stepDone) {
            doc.setVersion(doc.getVersion() + 1);
            doc.setUpdatedAt(now);
            documentRepository.update(doc);
            return;
        }

        Integer next = lines.stream()
                .filter(l -> ApprovalStatuses.isBlocking(l.getLineType()))
                .map(ApprovalLine::getStepOrder)
                .filter(s -> s > step)
                .min(Integer::compareTo)
                .orElse(null);

        if (next != null) {
            activateStep(lines, next);
            doc.setCurrentStep(next);
            doc.setStatus(ApprovalStatuses.IN_PROGRESS);
        } else {
            boolean allBlockingDone = lines.stream()
                    .filter(l -> ApprovalStatuses.isBlocking(l.getLineType()))
                    .allMatch(l -> ApprovalStatuses.LINE_APPROVED.equals(l.getStatus()));
            if (allBlockingDone) {
                doc.setStatus(ApprovalStatuses.APPROVED);
                doc.setCompletedAt(now);
                doc.setCurrentStep(null);
            }
        }
        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdatedAt(now);
        documentRepository.update(doc);
    }

    private void activateStep(List<ApprovalLine> lines, int step) {
        for (ApprovalLine line : lines) {
            if (ApprovalStatuses.isBlocking(line.getLineType()) && line.getStepOrder() == step
                    && (ApprovalStatuses.LINE_WAITING.equals(line.getStatus())
                    || ApprovalStatuses.LINE_PENDING.equals(line.getStatus()))) {
                line.setStatus(ApprovalStatuses.LINE_PENDING);
                line.setActive(true);
                lineRepository.update(line);
            }
        }
    }

    private void replaceLines(Long documentId, List<ApprovalDocumentRequest.ApprovalLineRequest> requests, boolean draft) {
        lineRepository.deleteByDocumentId(documentId);
        if (requests == null) {
            return;
        }
        int i = 0;
        for (ApprovalDocumentRequest.ApprovalLineRequest req : requests) {
            ApprovalLine line = new ApprovalLine();
            line.setDocumentId(documentId);
            line.setStepOrder(req.getStepOrder());
            line.setSortInStep(req.getSortInStep() > 0 ? req.getSortInStep() : i++);
            line.setLineType(req.getLineType().trim().toUpperCase());
            line.setApproverId(req.getApproverId());
            line.setStatus(draft ? ApprovalStatuses.LINE_WAITING : ApprovalStatuses.LINE_WAITING);
            line.setActive(false);
            lineRepository.insert(line);
        }
    }

    private void validateLines(Long drafterId, List<ApprovalDocumentRequest.ApprovalLineRequest> lines, boolean requireBlocking) {
        if (lines == null || lines.isEmpty()) {
            if (requireBlocking) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "결재선이 필요합니다.");
            }
            return;
        }
        Set<Long> people = new HashSet<>();
        Map<Integer, Set<String>> stepTypes = new HashMap<>();
        boolean hasBlocking = false;
        for (ApprovalDocumentRequest.ApprovalLineRequest line : lines) {
            String type = line.getLineType() == null ? "" : line.getLineType().trim().toUpperCase();
            if (!Set.of(ApprovalStatuses.TYPE_APPROVE, ApprovalStatuses.TYPE_AGREE,
                    ApprovalStatuses.TYPE_POST, ApprovalStatuses.TYPE_NOTIFY).contains(type)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "잘못된 결재선 유형입니다: " + type);
            }
            if (Objects.equals(line.getApproverId(), drafterId)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "자기결재는 허용되지 않습니다.");
            }
            if (!people.add(line.getApproverId())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "동일 사용자를 결재선에 중복 지정할 수 없습니다.");
            }
            User approver = userRepository.findById(line.getApproverId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결재 대상 사용자를 찾을 수 없습니다."));
            if (!approver.isEnabled()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "비활성 사용자는 지정할 수 없습니다: " + approver.getName());
            }
            if (ApprovalStatuses.isBlocking(type)) {
                hasBlocking = true;
            }
            stepTypes.computeIfAbsent(line.getStepOrder(), k -> new HashSet<>()).add(type);
        }
        for (Map.Entry<Integer, Set<String>> e : stepTypes.entrySet()) {
            Set<String> types = e.getValue();
            boolean hasApprove = types.contains(ApprovalStatuses.TYPE_APPROVE);
            boolean hasAgree = types.contains(ApprovalStatuses.TYPE_AGREE);
            if (hasApprove && hasAgree) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "같은 단계에 결재와 합의를 섞을 수 없습니다.");
            }
            boolean hasBlockingType = hasApprove || hasAgree;
            boolean hasNonBlocking = types.contains(ApprovalStatuses.TYPE_POST) || types.contains(ApprovalStatuses.TYPE_NOTIFY);
            if (hasBlockingType && hasNonBlocking) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "같은 단계에 결재/합의와 후결/통보를 섞을 수 없습니다.");
            }
        }
        if (requireBlocking && !hasBlocking) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "결재 또는 합의 라인이 최소 1개 필요합니다.");
        }
    }

    private void validatePersistedLines(Long drafterId, List<ApprovalLine> lines, boolean requireBlocking) {
        List<ApprovalDocumentRequest.ApprovalLineRequest> mapped = lines.stream().map(l -> {
            ApprovalDocumentRequest.ApprovalLineRequest r = new ApprovalDocumentRequest.ApprovalLineRequest();
            r.setStepOrder(l.getStepOrder());
            r.setSortInStep(l.getSortInStep());
            r.setLineType(l.getLineType());
            r.setApproverId(l.getApproverId());
            return r;
        }).toList();
        validateLines(drafterId, mapped, requireBlocking);
    }

    private ApprovalDocumentResponse toResponse(ApprovalDocument doc) {
        String drafterName = userRepository.findById(doc.getDrafterId()).map(User::getName).orElse(null);
        List<ApprovalDocumentResponse.ApprovalLineResponse> lines = lineRepository.findByDocumentId(doc.getId())
                .stream().map(ApprovalDocumentResponse::lineFrom).toList();
        List<ApprovalDocumentResponse.ApprovalHistoryResponse> histories = historyRepository.findByDocumentId(doc.getId())
                .stream().map(ApprovalDocumentResponse::historyFrom).toList();
        return ApprovalDocumentResponse.from(doc, drafterName, lines, histories);
    }

    private void addHistory(Long docId, Long actorId, String action, Integer step, Long lineId, String comment) {
        ApprovalHistory h = new ApprovalHistory();
        h.setDocumentId(docId);
        h.setActorId(actorId);
        h.setAction(action);
        h.setStepOrder(step);
        h.setLineId(lineId);
        h.setComment(comment);
        h.setCreatedAt(LocalDateTime.now());
        historyRepository.insert(h);
    }

    private String nextDocNo() {
        LocalDate today = LocalDate.now();
        documentRepository.upsertDocSeq(today);
        int seq = documentRepository.nextDocSeq(today);
        return "AP-" + today.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + String.format("%04d", seq);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다."));
    }

    private ApprovalDocument requireDoc(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결재 문서를 찾을 수 없습니다."));
    }

    private void assertDrafter(User me, ApprovalDocument doc) {
        if (!Objects.equals(me.getId(), doc.getDrafterId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "기안자만 가능합니다.");
        }
    }

    private void assertCanView(User me, ApprovalDocument doc) {
        if (Objects.equals(me.getId(), doc.getDrafterId())) {
            return;
        }
        boolean onLine = lineRepository.findByDocumentId(doc.getId()).stream()
                .anyMatch(l -> Objects.equals(l.getApproverId(), me.getId()));
        if (!onLine) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "문서에 접근할 수 없습니다.");
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
