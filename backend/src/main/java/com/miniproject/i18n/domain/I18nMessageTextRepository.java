package com.miniproject.i18n.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface I18nMessageTextRepository {

    Optional<I18nMessageText> findById(Long id);

    List<I18nMessageText> findByMessageId(Long messageId);

    List<I18nMessageText> findByMessageIdIn(@Param("messageIds") List<Long> messageIds);

    Optional<I18nMessageText> findByMessageIdAndLocaleId(@Param("messageId") Long messageId,
                                                         @Param("localeId") Long localeId);

    List<I18nMessageText> findBundleByLocale(@Param("localeCode") String localeCode);

    List<I18nMessageText> findBundleByLocaleAndGroup(@Param("localeCode") String localeCode,
                                                     @Param("groupCode") String groupCode);

    void deleteByMessageId(@Param("messageId") Long messageId);

    void deleteByGroupId(@Param("groupId") Long groupId);

    void deleteByLocaleId(@Param("localeId") Long localeId);

    int insert(I18nMessageText text);

    int update(I18nMessageText text);

    int deleteById(Long id);

    default I18nMessageText save(I18nMessageText text) {
        if (text.getId() == null) {
            insert(text);
        } else {
            update(text);
        }
        return text;
    }

    default void delete(I18nMessageText text) {
        deleteById(text.getId());
    }
}
