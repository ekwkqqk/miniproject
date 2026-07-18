package com.miniproject.i18n.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.i18n.domain.I18nLocale;
import com.miniproject.i18n.domain.I18nLocaleRepository;
import com.miniproject.i18n.domain.I18nMessage;
import com.miniproject.i18n.domain.I18nMessageGroup;
import com.miniproject.i18n.domain.I18nMessageGroupRepository;
import com.miniproject.i18n.domain.I18nMessageRepository;
import com.miniproject.i18n.domain.I18nMessageText;
import com.miniproject.i18n.domain.I18nMessageTextRepository;
import com.miniproject.i18n.dto.I18nGroupRequest;
import com.miniproject.i18n.dto.I18nLocaleRequest;
import com.miniproject.i18n.dto.I18nMessageRequest;
import com.miniproject.i18n.dto.I18nResolveRequest;
import com.miniproject.i18n.util.MessageFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class I18nService {

    private final I18nLocaleRepository localeRepository;
    private final I18nMessageGroupRepository groupRepository;
    private final I18nMessageRepository messageRepository;
    private final I18nMessageTextRepository textRepository;

    public I18nService(I18nLocaleRepository localeRepository,
                       I18nMessageGroupRepository groupRepository,
                       I18nMessageRepository messageRepository,
                       I18nMessageTextRepository textRepository) {
        this.localeRepository = localeRepository;
        this.groupRepository = groupRepository;
        this.messageRepository = messageRepository;
        this.textRepository = textRepository;
    }

    // --- Locale ---

    @Transactional(readOnly = true)
    public List<I18nLocaleRequest.Response> getAllLocales() {
        return localeRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(I18nLocaleRequest.Response::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<I18nLocaleRequest.Response> getEnabledLocales() {
        return localeRepository.findByEnabledTrueOrderBySortOrderAscIdAsc().stream()
                .map(I18nLocaleRequest.Response::from)
                .toList();
    }

    @Transactional
    public I18nLocaleRequest.Response createLocale(I18nLocaleRequest request) {
        String code = request.getCode().trim();
        if (localeRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 존재하는 로케일 코드입니다.");
        }
        I18nLocale locale = localeRepository.save(new I18nLocale(
                code,
                request.getName().trim(),
                request.getEnabled() == null || request.getEnabled(),
                request.getSortOrder() == null ? 0 : request.getSortOrder()
        ));
        return I18nLocaleRequest.Response.from(locale);
    }

    @Transactional
    public I18nLocaleRequest.Response updateLocale(Long id, I18nLocaleRequest request) {
        I18nLocale locale = localeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "로케일을 찾을 수 없습니다."));
        locale.changeName(request.getName().trim());
        if (request.getEnabled() != null) {
            locale.changeEnabled(request.getEnabled());
        }
        if (request.getSortOrder() != null) {
            locale.changeSortOrder(request.getSortOrder());
        }
        localeRepository.save(locale);
        return I18nLocaleRequest.Response.from(locale);
    }

    @Transactional
    public void deleteLocale(Long id) {
        I18nLocale locale = localeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "로케일을 찾을 수 없습니다."));
        textRepository.deleteByLocaleId(id);
        localeRepository.delete(locale);
    }

    // --- Group ---

    @Transactional(readOnly = true)
    public List<I18nGroupRequest.Response> getAllGroups() {
        return groupRepository.findAllByOrderByIdAsc().stream()
                .map(I18nGroupRequest.Response::from)
                .toList();
    }

    @Transactional
    public I18nGroupRequest.Response createGroup(I18nGroupRequest request) {
        String code = request.getCode().trim();
        if (groupRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 존재하는 그룹 코드입니다.");
        }
        I18nMessageGroup group = groupRepository.save(new I18nMessageGroup(
                code, request.getName().trim(), request.getDescription()
        ));
        return I18nGroupRequest.Response.from(group);
    }

    @Transactional
    public I18nGroupRequest.Response updateGroup(Long id, I18nGroupRequest request) {
        I18nMessageGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메시지 그룹을 찾을 수 없습니다."));
        group.changeName(request.getName().trim());
        group.changeDescription(request.getDescription());
        groupRepository.save(group);
        return I18nGroupRequest.Response.from(group);
    }

    @Transactional
    public void deleteGroup(Long id) {
        I18nMessageGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메시지 그룹을 찾을 수 없습니다."));
        textRepository.deleteByGroupId(id);
        messageRepository.deleteByGroupId(id);
        groupRepository.delete(group);
    }

    // --- Message ---

    @Transactional(readOnly = true)
    public List<I18nMessageRequest.Response> getMessages(String groupCode) {
        List<I18nMessage> messages = groupCode == null || groupCode.isBlank()
                ? messageRepository.findAllWithGroup()
                : messageRepository.findByGroupCode(groupCode.trim());
        return toMessageResponses(messages);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMessagesPage(String groupCode, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;
        String normalizedGroup = blankToNull(groupCode);

        List<I18nMessageRequest.Response> items = toMessageResponses(
                messageRepository.findPage(normalizedGroup, safeSize, offset)
        );
        long total = messageRepository.count(normalizedGroup);

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("page", safePage);
        result.put("size", safeSize);
        result.put("total", total);
        return result;
    }

    private List<I18nMessageRequest.Response> toMessageResponses(List<I18nMessage> messages) {
        if (messages.isEmpty()) {
            return List.of();
        }
        List<Long> messageIds = messages.stream().map(I18nMessage::getId).toList();
        Map<Long, Map<String, String>> textsByMessageId = textRepository.findByMessageIdIn(messageIds).stream()
                .collect(Collectors.groupingBy(
                        I18nMessageText::getMessageId,
                        Collectors.toMap(
                                t -> t.getLocale().getCode(),
                                I18nMessageText::getText,
                                (a, b) -> a,
                                LinkedHashMap::new
                        )
                ));
        return messages.stream()
                .map(message -> I18nMessageRequest.Response.of(
                        message,
                        textsByMessageId.getOrDefault(message.getId(), new LinkedHashMap<>())
                ))
                .toList();
    }

    @Transactional
    public I18nMessageRequest.Response createMessage(I18nMessageRequest request) {
        I18nMessageGroup group = groupRepository.findByCode(request.getGroupCode().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메시지 그룹을 찾을 수 없습니다."));
        String code = request.getCode().trim();
        if (messageRepository.existsByGroupIdAndCode(group.getId(), code)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "그룹 내 이미 존재하는 메시지 코드입니다.");
        }
        I18nMessage message = messageRepository.save(new I18nMessage(group, code, request.getDescription()));
        saveTexts(message, request.getTexts());
        return toMessageResponse(message);
    }

    @Transactional
    public I18nMessageRequest.Response updateMessage(Long id, I18nMessageRequest request) {
        I18nMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));
        message.changeDescription(request.getDescription());
        messageRepository.save(message);
        saveTexts(message, request.getTexts());
        return toMessageResponse(message);
    }

    @Transactional
    public void deleteMessage(Long id) {
        I18nMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));
        textRepository.deleteByMessageId(id);
        messageRepository.delete(message);
    }

    // --- Bundle / Resolve ---

    @Transactional(readOnly = true)
    public Map<String, String> getMessageBundle(String localeCode, String groupCode) {
        String group = blankToNull(groupCode);
        List<I18nMessageText> texts = group == null
                ? textRepository.findBundleByLocale(localeCode)
                : textRepository.findBundleByLocaleAndGroup(localeCode, group);
        Map<String, String> bundle = new LinkedHashMap<>();
        for (I18nMessageText text : texts) {
            String key = text.getMessage().getGroup().getCode() + "." + text.getMessage().getCode();
            bundle.put(key, text.getText());
        }
        return bundle;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> resolve(I18nResolveRequest request) {
        I18nMessageGroup group = groupRepository.findByCode(request.getGroup().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메시지 그룹을 찾을 수 없습니다."));
        I18nMessage message = messageRepository.findByGroupIdAndCode(group.getId(), request.getCode().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));
        I18nLocale locale = localeRepository.findByCode(request.getLocale().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "로케일을 찾을 수 없습니다."));
        I18nMessageText text = textRepository.findByMessageIdAndLocaleId(message.getId(), locale.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 로케일 메시지가 없습니다."));
        String resolved = MessageFormatter.format(text.getText(), request.getParams());
        return Map.of(
                "group", group.getCode(),
                "code", message.getCode(),
                "locale", locale.getCode(),
                "template", text.getText(),
                "message", resolved
        );
    }

    private void saveTexts(I18nMessage message, Map<String, String> texts) {
        if (texts == null) {
            return;
        }
        for (Map.Entry<String, String> entry : texts.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }
            I18nLocale locale = localeRepository.findByCode(entry.getKey().trim())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                            "로케일을 찾을 수 없습니다: " + entry.getKey()));
            String value = entry.getValue() == null ? "" : entry.getValue();
            textRepository.findByMessageIdAndLocaleId(message.getId(), locale.getId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.changeText(value);
                                textRepository.save(existing);
                            },
                            () -> textRepository.save(new I18nMessageText(message, locale, value))
                    );
        }
    }

    private I18nMessageRequest.Response toMessageResponse(I18nMessage message) {
        Map<String, String> texts = textRepository.findByMessageId(message.getId()).stream()
                .collect(Collectors.toMap(
                        t -> t.getLocale().getCode(),
                        I18nMessageText::getText,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return I18nMessageRequest.Response.of(message, texts);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
