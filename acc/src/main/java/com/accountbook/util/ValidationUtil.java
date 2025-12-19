package com.accountbook.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeParseException;

/**
 * 프로젝트 요구사항에 따른 입력 유효성 검사를 위한 유틸리티 클래스입니다.
 */
public class ValidationUtil {
    
    // 최소 날짜 기준 (2025-10-01)
    private static final LocalDate MIN_DATE = LocalDate.of(2025, 10, 1);
    // 최대 금액 (100,000,000)
    private static final int MAX_AMOUNT = 100_000_000;
    // 최대 설명 길이 (50자)
    private static final int MAX_DESCRIPTION_LENGTH = 50;
    // 날짜 형식 지정
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    
    /**
     * 요구사항에 따라 날짜 입력을 검증합니다:
     * - 형식: YYYY-MM-DD
     * - 범위: 2025-10-01 이후여야 함
     * * NOTE: Java의 LocalDate.parse()는 "4월 31일"과 같은 달력에 존재하지 않는 날짜를 입력하면 
     * DateTimeParseException을 자동으로 발생시켜 (날짜 존재) 유효성 검사를 수행합니다.
     */
    public static ValidationResult validateDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return new ValidationResult(false, "날짜는 비워둘 수 없습니다.");
        }
        
        try {
            // 1. 형식 및 달력 유효성 검사 (29,30/31일 자동 검증)
            LocalDate date = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            
            // 2. 범위 검사
            if (date.isBefore(MIN_DATE) || date.isEqual(MIN_DATE)) {
                return new ValidationResult(false, "날짜는 2025-10-01 이후여야 합니다.");
            }
            
            return new ValidationResult(true, null, date);
        } catch (DateTimeParseException e) {
            // [수정된 부분]: 오류 메시지를 더 명확하게 표시
            return new ValidationResult(false, "날짜 입력 오류: YYYY-MM-DD 형식이 아니거나 달력에 존재하지 않는 날짜입니다. ");
        }
    }
    
    /**
     * 요구사항에 따라 금액 입력을 검증합니다:
     * - 양의 정수만 가능
     * - 선행 0, 쉼표 또는 숫자가 아닌 문자는 허용되지 않음
     * - 최대값: 100,000,000 (1억원 까지)
     */
    public static ValidationResult validateAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return new ValidationResult(false, "금액은 비워둘 수 없습니다.");
        }
        
        String trimmed = amountStr.trim();
        
        // 선행 0 확인 ("0" 제외)
        if (trimmed.length() > 1 && trimmed.startsWith("0")) {
            return new ValidationResult(false, "금액은 선행 0을 가질 수 없습니다.");
        }
        
        try {
            int amount = Integer.parseInt(trimmed);
            
            if (amount <= 0) {
                return new ValidationResult(false, "금액은 양수여야 합니다.");
            }
            
            if (amount > MAX_AMOUNT) {
                return new ValidationResult(false, "금액은 100,000,000을 초과할 수 없습니다.");
            }
            
            return new ValidationResult(true, null, amount);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "금액은 유효한 양의 정수여야 합니다.");
        }
    }
    
    /**
     * 요구사항에 따라 설명 입력을 검증합니다:
     * - 문자열, 최대 50자
     * - 특수문자로만 구성될 수 없음
     */
    public static ValidationResult validateDescription(String description) {
        if (description == null) {
            return new ValidationResult(true, null, "");
        }
        
        String trimmed = description.trim();
        
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            return new ValidationResult(false, "설명은 50자를 초과할 수 없습니다.");
        }
        
        // 설명이 특수문자로만 구성되었는지 확인 (한글, 영문, 숫자, 공백 허용)
        if (!trimmed.isEmpty() && trimmed.matches("^[^a-zA-Z0-9\\s가-힣]+$")) {
            return new ValidationResult(false, "설명은 특수문자로만 구성될 수 없습니다.");
        }
        
        return new ValidationResult(true, null, trimmed);
    }
    
    /**
     * 메뉴 옵션 입력을 검증합니다.
     */
    public static ValidationResult validateMenuOption(String input, int minOption, int maxOption) {
        if (input == null || input.trim().isEmpty()) {
            return new ValidationResult(false, "유효한 옵션을 입력해주세요.");
        }
        
        try {
            int option = Integer.parseInt(input.trim());
            
            if (option < minOption || option > maxOption) {
                return new ValidationResult(false, 
                    String.format("숫자를 %d와 %d 사이로 입력해주세요.", minOption, maxOption));
            }
            
            return new ValidationResult(true, null, option);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "유효한 숫자를 입력해주세요.");
        }
    }
    
    /**
     * 유효성 검사 작업 결과를 위한 클래스
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final Object value;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.value = null;
        }
        
        public ValidationResult(boolean valid, String errorMessage, Object value) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.value = value;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public Object getValue() {
            return value;
        }
        
        @SuppressWarnings("unchecked")
        public <T> T getValue(Class<T> type) {
            return (T) value;
        }
    }
}