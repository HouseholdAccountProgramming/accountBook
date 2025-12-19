package com.accountbook.util;

import com.accountbook.model.LedgerItem;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class CsvFileHandler {
    
    private static final String DEFAULT_FILE_NAME = "ledger.csv";
    private static final String CSV_HEADER = "id,type,date,category,amount,description";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    
    private final String fileName;
    
    public CsvFileHandler() {
        this.fileName = DEFAULT_FILE_NAME;
    }
    
    public CsvFileHandler(String fileName) {
        this.fileName = fileName;
    }
    
    public List<LedgerItem> loadFromFile() {
        List<LedgerItem> items = new ArrayList<>();
        File file = new File(fileName);
        
        if (!file.exists()) {
            System.out.println("기존 데이터 파일이 없습니다. 빈 가계부로 시작합니다.");
            return items;
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            
            if (line == null || !line.equals(CSV_HEADER)) {
                System.out.println("경고: 유효하지 않거나 누락된 CSV 헤더입니다. 빈 가계부로 시작합니다.");
                return items;
            }
            
            int lineNumber = 2;
            while ((line = reader.readLine()) != null) {
                try {
                    LedgerItem item = parseCsvLine(line);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    System.out.printf("경고: %d번 줄의 유효하지 않은 항목을 건너킵니다: %s%n", lineNumber, e.getMessage());
                }
                lineNumber++;
            }
            
            System.out.printf("%s에서 %d개의 항목을 불러왔습니다.%n", fileName, items.size());
            
        } catch (IOException e) {
            System.err.printf("파일 %s 읽기 오류: %s%n", fileName, e.getMessage());
        }
        
        return items;
    }
    
    public boolean saveToFile(List<LedgerItem> items) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            writer.println(CSV_HEADER);
            
            for (LedgerItem item : items) {
                writer.println(formatCsvLine(item));
            }
            
            System.out.printf("%s에 %d개의 항목을 저장했습니다.%n", fileName, items.size());
            return true;
            
        } catch (IOException e) {
            System.err.printf("파일 %s 쓰기 오류: %s%n", fileName, e.getMessage());
            return false;
        }
    }
    
    private LedgerItem parseCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = line.split(",", -1);
        
        if (parts.length != 6) {
            throw new IllegalArgumentException("유효하지 않은 CSV 형식: 6개의 필드가 필요하지만, " + parts.length + "개가 발견되었습니다.");
        }
        
        try {
            int id = Integer.parseInt(parts[0].trim());
            
            // 파싱 시 '수입 (+)' 형식 제거 및 순수한 유형 추출 로직 추가
            String rawType = parts[1].trim();
            String pureType;
            
            if (rawType.contains("수입")) {
                pureType = "수입";
            } else if (rawType.contains("지출")) {
                pureType = "지출";
            } else {
                 throw new IllegalArgumentException("유효하지 않은 유형: " + rawType);
            }
            
            LocalDate date = LocalDate.parse(parts[2].trim(), DATE_FORMATTER);
            String category = parts[3].trim();
            
            String amountString = parts[4].trim();
            amountString = amountString.replaceAll(",", "");
            int amount = Integer.parseInt(amountString);
            
            String descriptionRaw = parts[5].trim();
            String description = descriptionRaw.isEmpty() ? null : descriptionRaw;
            
            // 엄격 유효성 검사 (순수한 type 사용)
            if (!("수입".equals(pureType) || "지출".equals(pureType))) { 
                 throw new IllegalArgumentException("유효하지 않은 유형: " + pureType); 
            }
            if (!date.isAfter(LocalDate.of(2025, 10, 1))) {
                throw new IllegalArgumentException("날짜는 2025-10-01 이후여야 합니다.");
            }
            int absoluteAmount = Math.abs(amount); 
            if (absoluteAmount <= 0 || absoluteAmount > 100_000_000) {
                throw new IllegalArgumentException("유효하지 않은 금액: " + amount);
            }
            if (!CategoryManager.isValidCategory(category)) {
                throw new IllegalArgumentException("유효하지 않은 카테고리: " + category);
            }
            if (description != null) {
                if (description.length() > 50) {
                    throw new IllegalArgumentException("설명이 50자를 초과합니다.");
                }
                if (!description.isEmpty() && description.matches("^[^a-zA-Z0-9\\s가-힣]+$")) {
                    throw new IllegalArgumentException("설명은 특수문자로만 구성될 수 없습니다.");
                }
            }
            
            // LedgerItem 생성 시, LedgerService에서 요구하는 형식으로 Type을 다시 맞춥니다.
            String finalType = pureType.equals("수입") ? "수입 (+)" : " 지출 (-)";
            
            return new LedgerItem(id, finalType, date, amount, category, description); 
            
        } catch (Exception e) {
            throw new IllegalArgumentException("CSV 줄 파싱 오류: " + e.getMessage());
        }
    }
    
    private String formatCsvLine(LedgerItem item) {
        return String.format("%d,%s,%s,%s,%d,%s",
            item.getId(),
            item.getType(),
            item.getDate().format(DATE_FORMATTER),
            item.getCategory(),
            item.getAmount(),
            item.getDescription() != null ? item.getDescription() : ""
        );
    }
    
    public boolean fileExists() {
        return new File(fileName).exists();
    }
    
    public String getFileName() {
        return fileName;
    }
}