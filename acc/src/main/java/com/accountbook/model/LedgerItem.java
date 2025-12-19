package com.accountbook.model;

import java.time.LocalDate;

/**
 * 개인 가계부의 핵심 데이터 엔티티(entity)인 가계부 항목을 나타냅니다.
 * 유효성 검사 로직과 미리 정의된 카테고리를 포함합니다.
 */
public class LedgerItem {
    
    
    // 고유 식별자 (자동 증가)
    private int id;
    // 항목 유형: "수입" 또는 "지출"
    private String type;
    
    // 거래 날짜: 유효해야 하며 2025-10-01 이후여야 함
    private LocalDate date;
    
    // 금액: 1억 이하의 양의 정수
    private int amount;
    
    // 카테고리: 미리 정의된 값 중 하나여야 함
    private String category;
    
    // 내용(설명): 최대 길이 50자의 문자열
    private String description;
    
    // 기본 생성자
    public LedgerItem() {}
    
    // 모든 필드를 포함하는 생성자
    public LedgerItem(int id, String type, LocalDate date, int amount, String category, String description) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.description = description;
    }
    
    // ID가 없는 생성자 (새 항목용)
    public LedgerItem(String type, LocalDate date, int amount, String category, String description) {
        this.type = type;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.description = description;
    }
    
    // Getter와 Setter
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return String.format("%d | %s | %s | %s | %d | %s",
            id, type, date, category, amount, description != null ? description : "");
    }
}