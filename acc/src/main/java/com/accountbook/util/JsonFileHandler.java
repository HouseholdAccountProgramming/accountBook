package com.accountbook.util;

import com.accountbook.model.LedgerItem;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

/**
 * 가계부 데이터의 영속성을 위한 JSON 파일 작업을 처리합니다.
 */
public class JsonFileHandler {
    
    private static final String DEFAULT_FILE_NAME = "ledger.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    
    private final String fileName;
    
    public JsonFileHandler() {
        this.fileName = DEFAULT_FILE_NAME;
    }
    
    public JsonFileHandler(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     * JSON 파일에서 가계부 항목을 불러옵니다.
     * 파일이 존재하지 않거나 오류가 있으면 빈 목록을 반환합니다.
     */
    public List<LedgerItem> loadFromFile() {
        List<LedgerItem> items = new ArrayList<>();
        File file = new File(fileName);
        
        if (!file.exists()) {
            System.out.println("기존 데이터 파일이 없습니다. 빈 가계부로 시작합니다.");
            return items;
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            
            String content = jsonContent.toString().trim();
            if (content.isEmpty()) {
                System.out.println("경고: 빈 JSON 파일입니다. 빈 가계부로 시작합니다.");
                return items;
            }
            
            items = parseJsonContent(content);
            System.out.printf("%s에서 %d개의 항목을 불러왔습니다.%n", fileName, items.size());
            
        } catch (IOException e) {
            System.err.printf("파일 %s 읽기 오류: %s%n", fileName, e.getMessage());
        } catch (Exception e) {
            System.err.printf("JSON 파싱 오류: %s. 빈 가계부로 시작합니다.%n", e.getMessage());
        }
        
        return items;
    }
    
    /**
     * 가계부 항목 목록을 JSON 파일에 저장합니다.
     */
    public boolean saveToFile(List<LedgerItem> items) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            String jsonContent = formatJsonContent(items);
            writer.print(jsonContent);
            
            System.out.printf("%s에 %d개의 항목을 저장했습니다.%n", fileName, items.size());
            return true;
            
        } catch (IOException e) {
            System.err.printf("파일 %s 쓰기 오류: %s%n", fileName, e.getMessage());
            return false;
        }
    }
    
    /**
     * JSON 내용을 파싱하여 LedgerItem 목록으로 변환합니다.
     */
    private List<LedgerItem> parseJsonContent(String content) {
        List<LedgerItem> items = new ArrayList<>();
        
        // 간단한 JSON 파싱 (외부 라이브러리 없이)
        content = content.trim();
        if (!content.startsWith("{") || !content.endsWith("}")) {
            throw new IllegalArgumentException("유효하지 않은 JSON 형식: 객체가 아닙니다.");
        }
        
        // "items" 배열 찾기
        String itemsKey = "\"items\"";
        int itemsIndex = content.indexOf(itemsKey);
        if (itemsIndex == -1) {
            throw new IllegalArgumentException("JSON에서 'items' 배열을 찾을 수 없습니다.");
        }
        
        int arrayStart = content.indexOf("[", itemsIndex);
        int arrayEnd = content.lastIndexOf("]");
        
        if (arrayStart == -1 || arrayEnd == -1 || arrayStart >= arrayEnd) {
            throw new IllegalArgumentException("유효하지 않은 JSON 배열 형식입니다.");
        }
        
        String arrayContent = content.substring(arrayStart + 1, arrayEnd).trim();
        if (arrayContent.isEmpty()) {
            return items; // 빈 배열
        }
        
        // 각 객체 파싱
        List<String> objectStrings = splitJsonObjects(arrayContent);
        for (int i = 0; i < objectStrings.size(); i++) {
            try {
                LedgerItem item = parseJsonObject(objectStrings.get(i));
                if (item != null) {
                    items.add(item);
                }
            } catch (Exception e) {
                System.out.printf("경고: %d번째 JSON 객체의 유효하지 않은 항목을 건너뜁니다: %s%n", i + 1, e.getMessage());
            }
        }
        
        return items;
    }
    
    /**
     * JSON 배열 내용을 개별 객체 문자열로 분할합니다.
     */
    private List<String> splitJsonObjects(String arrayContent) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0;
        int start = 0;
        
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    objects.add(arrayContent.substring(start, i + 1).trim());
                    start = i + 1;
                    // 다음 객체까지 쉼표 건너뛰기
                    while (start < arrayContent.length() && 
                           (arrayContent.charAt(start) == ',' || Character.isWhitespace(arrayContent.charAt(start)))) {
                        start++;
                    }
                    i = start - 1;
                }
            }
        }
        
        return objects;
    }
    
    /**
     * 단일 JSON 객체를 LedgerItem으로 파싱합니다.
     */
    private LedgerItem parseJsonObject(String objectStr) {
        objectStr = objectStr.trim();
        if (!objectStr.startsWith("{") || !objectStr.endsWith("}")) {
            throw new IllegalArgumentException("유효하지 않은 JSON 객체 형식");
        }
        
        // 중괄호 제거
        String content = objectStr.substring(1, objectStr.length() - 1);
        
        // 필드 파싱
        Integer id = null;
        String type = null;
        LocalDate date = null;
        String category = null;
        Integer amount = null;
        String description = null;
        
        String[] pairs = splitJsonPairs(content);
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) continue;
            
            String key = keyValue[0].trim().replaceAll("\"", "");
            String value = keyValue[1].trim();
            
            // 문자열 값에서 따옴표 제거
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            
            switch (key) {
                case "id":
                    id = Integer.parseInt(value);
                    break;
                case "type":
                    type = value;
                    break;
                case "date":
                    date = LocalDate.parse(value, DATE_FORMATTER);
                    break;
                case "category":
                    category = value;
                    break;
                case "amount":
                    amount = Integer.parseInt(value);
                    break;
                case "description":
                    description = value.isEmpty() ? null : value;
                    break;
            }
        }
        
        // 필수 필드 검증
        if (id == null || type == null || date == null || category == null || amount == null) {
            throw new IllegalArgumentException("필수 필드가 누락되었습니다.");
        }
        
        // 기본 유효성 검사
        if (!("수입".equals(type) || "지출".equals(type))) {
            throw new IllegalArgumentException("유효하지 않은 유형: " + type);
        }
        if (!date.isAfter(LocalDate.of(2025, 10, 1))) {
            throw new IllegalArgumentException("날짜는 2025-10-01 이후여야 합니다.");
        }
        if (!CategoryManager.isValidCategory(category)) {
            throw new IllegalArgumentException("유효하지 않은 카테고리: " + category);
        }
        if (amount <= 0 || amount > 100_000_000) {
            throw new IllegalArgumentException("유효하지 않은 금액: " + amount);
        }
        if (description != null) {
            if (description.length() > 50) {
                throw new IllegalArgumentException("설명이 50자를 초과합니다.");
            }
            if (!description.isEmpty() && description.matches("^[^a-zA-Z0-9\\s가-힣]+$")) {
                throw new IllegalArgumentException("설명은 특수문자로만 구성될 수 없습니다.");
            }
        }
        
        return new LedgerItem(id, type, date, amount, category, description);
    }
    
    /**
     * JSON 객체 내용을 키-값 쌍으로 분할합니다.
     */
    private String[] splitJsonPairs(String content) {
        List<String> pairs = new ArrayList<>();
        boolean inQuotes = false;
        int start = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                pairs.add(content.substring(start, i).trim());
                start = i + 1;
            }
        }
        
        if (start < content.length()) {
            pairs.add(content.substring(start).trim());
        }
        
        return pairs.toArray(new String[0]);
    }
    
    /**
     * LedgerItem 목록을 JSON 형식으로 포맷합니다.
     */
    private String formatJsonContent(List<LedgerItem> items) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"items\": [\n");
        
        for (int i = 0; i < items.size(); i++) {
            LedgerItem item = items.get(i);
            json.append("    {\n");
            json.append(String.format("      \"id\": %d,\n", item.getId()));
            json.append(String.format("      \"type\": \"%s\",\n", item.getType()));
            json.append(String.format("      \"date\": \"%s\",\n", item.getDate().format(DATE_FORMATTER)));
            json.append(String.format("      \"category\": \"%s\",\n", item.getCategory()));
            json.append(String.format("      \"amount\": %d,\n", item.getAmount()));
            json.append(String.format("      \"description\": \"%s\"\n", item.getDescription() != null ? item.getDescription() : ""));
            json.append("    }");
            
            if (i < items.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ]\n}");
        return json.toString();
    }
    
    /**
     * 데이터 파일이 존재하는지 확인합니다.
     */
    public boolean fileExists() {
        return new File(fileName).exists();
    }
    
    /**
     * 사용 중인 파일명을 가져옵니다.
     */
    public String getFileName() {
        return fileName;
    }
}