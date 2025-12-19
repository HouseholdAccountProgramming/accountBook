package com.accountbook.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 카테고리 관리를 위한 전역 매니저.
 * 기본 고정 6개 + 커스텀 최대 4개(총 10개) 지원.
 * 번호는 화면에서 1부터 순서대로 표시하며, 커스텀 삭제 시 자동으로 재정렬됩니다
 * (리스트 순서에 따라 번호가 자연스럽게 변경됨).
 */
public final class CategoryManager {
    private CategoryManager() {}

    // 고정 카테고리 (변경 불가)
    private static final List<String> FIXED = Arrays.asList(
        "Food", "Transport", "Living", "Shopping", "Transfer", "Hobby"
    );

    // 커스텀 카테고리 (변경 가능, 최대 4개)
    private static final List<String> CUSTOM = new ArrayList<>();

    public static List<String> getFixedCategories() {
        return Collections.unmodifiableList(FIXED);
    }

    public static List<String> getCustomCategories() {
        return Collections.unmodifiableList(CUSTOM);
    }

    /** 모든 카테고리(고정 + 커스텀)를 순서대로 반환 */
    public static List<String> getAllCategories() {
        List<String> all = new ArrayList<>(FIXED.size() + CUSTOM.size());
        all.addAll(FIXED);
        all.addAll(CUSTOM);
        return all;
    }

    /** 카테고리 이름이 현재 목록에 존재하는지 */
    public static boolean isValidCategory(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return !trimmed.isEmpty() && getAllCategories().stream().anyMatch(c -> c.equals(trimmed));
    }

    /** 커스텀 카테고리 추가 (최대 4개, 중복 불가) */
    public static AddResult addCustomCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            return AddResult.error("카테고리 이름을 입력해주세요.");
        }
        String trimmed = name.trim();

        if (getAllCategories().contains(trimmed)) {
            return AddResult.error("이미 존재하는 카테고리입니다.");
        }

        if (CUSTOM.size() >= 4) {
            return AddResult.error("카테고리는 최대 4개까지만 추가가 가능합니다.");
        }

        CUSTOM.add(trimmed);
        return AddResult.ok(trimmed);
    }

    /** 커스텀 카테고리 삭제 (고정 카테고리는 삭제 불가) */
    public static DeleteResult deleteCustomCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            return DeleteResult.error("카테고리 이름을 입력해주세요.");
        }
        String trimmed = name.trim();

        if (FIXED.contains(trimmed)) {
            return DeleteResult.error("고정 카테고리는 삭제할 수 없습니다.");
        }

        boolean removed = CUSTOM.remove(trimmed);
        if (!removed) {
            return DeleteResult.error("해당 카테고리 항목이 없습니다.");
        }
        // 리스트에서 제거되면 자동으로 재정렬 (번호는 화면에서 1부터 다시 매겨짐)
        return DeleteResult.ok(trimmed);
    }

    // 결과 타입들
    public static class AddResult {
        public final boolean success;
        public final String message;
        public final String addedName;
        private AddResult(boolean success, String message, String addedName) {
            this.success = success; this.message = message; this.addedName = addedName;
        }
        public static AddResult ok(String name) { return new AddResult(true, null, name); }
        public static AddResult error(String msg) { return new AddResult(false, msg, null); }
    }

    public static class DeleteResult {
        public final boolean success;
        public final String message;
        public final String deletedName;
        private DeleteResult(boolean success, String message, String deletedName) {
            this.success = success; this.message = message; this.deletedName = deletedName;
        }
        public static DeleteResult ok(String name) { return new DeleteResult(true, null, name); }
        public static DeleteResult error(String msg) { return new DeleteResult(false, msg, null); }
    }
}