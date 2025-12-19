package com.accountbook.service;

import com.accountbook.model.LedgerItem;
import com.accountbook.util.CsvFileHandler;
import com.accountbook.util.JsonFileHandler;
import com.accountbook.util.FileFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 가계부 작업(CRUD)을 관리하기 위한 서비스 클래스입니다.
 */
public class LedgerService {
    
    private List<LedgerItem> items;
    private CsvFileHandler csvFileHandler;
    private JsonFileHandler jsonFileHandler;
    private FileFormat currentFormat;
    private String baseFileName;
    private int nextId;
    
    public LedgerService() {
        this.currentFormat = FileFormat.CSV; // 기본값은 CSV
        this.baseFileName = "ledger";
        initializeFileHandlers();
        this.items = new ArrayList<>();
        this.nextId = 1;
        loadData();
    }
    
    public LedgerService(String fileName) {
        this.baseFileName = getBaseFileName(fileName);
        this.currentFormat = FileFormat.fromFileName(fileName);
        if (this.currentFormat == null) {
            this.currentFormat = FileFormat.CSV; // 기본값
        }
        initializeFileHandlers();
        this.items = new ArrayList<>();
        this.nextId = 1;
        loadData();
    }
    
    public LedgerService(FileFormat format) {
        this.currentFormat = format;
        this.baseFileName = "ledger";
        initializeFileHandlers();
        this.items = new ArrayList<>();
        this.nextId = 1;
        loadData();
    }
    
    public LedgerService(String baseFileName, FileFormat format) {
        this.baseFileName = getBaseFileName(baseFileName);
        this.currentFormat = format;
        initializeFileHandlers();
        this.items = new ArrayList<>();
        this.nextId = 1;
        loadData();
    }
    
    /**
     * 파일 핸들러들을 초기화합니다.
     */
    private void initializeFileHandlers() {
        String csvFileName = baseFileName + ".csv";
        String jsonFileName = baseFileName + ".json";
        this.csvFileHandler = new CsvFileHandler(csvFileName);
        this.jsonFileHandler = new JsonFileHandler(jsonFileName);
    }
    
    /**
     * 파일명에서 확장자를 제거한 기본 이름을 추출합니다.
     */
    private String getBaseFileName(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return fileName != null ? fileName : "ledger";
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
    
    /**
     * 시작 시 파일에서 데이터를 불러옵니다.
     */
    private void loadData() {
        switch (currentFormat) {
            case CSV:
                items = csvFileHandler.loadFromFile();
                break;
            case JSON:
                items = jsonFileHandler.loadFromFile();
                break;
            default:
                items = new ArrayList<>();
        }
        
        // 기존 항목을 기반으로 다음 ID를 계산
        if (!items.isEmpty()) {
            nextId = items.stream()
                .mapToInt(LedgerItem::getId)
                .max()
                .orElse(0) + 1;
        }
    }
    
    /**
     * 가계부에 새 항목을 추가합니다.
     */
    public boolean addItem(String type, LocalDate date, int amount, String category, String description) {
        LedgerItem newItem = new LedgerItem(nextId, type, date, amount, category, description);
        items.add(newItem);
        nextId++;
        
        boolean saved = saveData();
        if (saved) {
            System.out.printf("항목이 ID: %d로 성공적으로 추가되었습니다.%n", newItem.getId());
        }
        return saved;
    }
    
    /**
     * ID로 항목을 삭제합니다.
     */
    public boolean deleteItem(int id) {
        boolean removed = items.removeIf(item -> item.getId() == id);
        
        if (!removed) {
            System.out.printf("ID %d를 가진 항목이 존재하지 않습니다.%n", id);
            return false;
        }
        
        boolean saved = saveData();
        if (saved) {
            System.out.printf("ID %d를 가진 항목이 성공적으로 삭제되었습니다.%n", id);
        }
        return saved;
    }
    
    /**
     * 모든 항목을 ID별로 정렬(오름차순)하여 가져옵니다.
     */
    public List<LedgerItem> getAllItems() {
        return items.stream()
            .sorted(Comparator.comparingInt(LedgerItem::getId))
            .collect(Collectors.toList());
    }
    
    /**
     * 날짜 범위 내의 항목을 가져옵니다.
     */
    public List<LedgerItem> getItemsByDateRange(LocalDate startDate, LocalDate endDate) {
        return items.stream()
            .filter(item -> {
                LocalDate itemDate = item.getDate();
                return !itemDate.isBefore(startDate) && !itemDate.isAfter(endDate);
            })
            .sorted(Comparator.comparingInt(LedgerItem::getId))
            .collect(Collectors.toList());
    }
    
    /**
     * 카테고리별 항목을 가져옵니다.
     */
    public List<LedgerItem> getItemsByCategory(String category) {
        return items.stream()
            .filter(item -> item.getCategory().equals(category))
            .sorted(Comparator.comparingInt(LedgerItem::getId))
            .collect(Collectors.toList());
    }
    
    /**
     * 주어진 ID를 가진 항목이 존재하는지 확인합니다.
     */
    public boolean itemExists(int id) {
        return items.stream().anyMatch(item -> item.getId() == id);
    }
    
    /**
     * 전체 항목 수를 가져옵니다.
     */
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * 데이터를 수동으로 파일에 저장합니다.
     */
    public boolean saveData() {
        switch (currentFormat) {
            case CSV:
                return csvFileHandler.saveToFile(items);
            case JSON:
                return jsonFileHandler.saveToFile(items);
            default:
                return false;
        }
    }
    
    /**
     * 데이터를 수동으로 파일에서 불러옵니다.
     */
    public boolean loadData(boolean overwrite) {
        if (!overwrite && !items.isEmpty()) {
            System.out.println("이미 데이터가 불러와져 있습니다. 다시 불러오려면 overwrite=true를 사용하세요.");
            return false;
        }
        
        List<LedgerItem> loadedItems;
        switch (currentFormat) {
            case CSV:
                loadedItems = csvFileHandler.loadFromFile();
                break;
            case JSON:
                loadedItems = jsonFileHandler.loadFromFile();
                break;
            default:
                loadedItems = null;
        }
        
        if (loadedItems != null) {
            items = loadedItems;
            
            // 다음 ID 재계산
            if (!items.isEmpty()) {
                nextId = items.stream()
                    .mapToInt(LedgerItem::getId)
                    .max()
                    .orElse(0) + 1;
            } else {
                nextId = 1;
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 영속성에 사용되는 파일명을 가져옵니다.
     */
    public String getFileName() {
        switch (currentFormat) {
            case CSV:
                return csvFileHandler.getFileName();
            case JSON:
                return jsonFileHandler.getFileName();
            default:
                return baseFileName + ".csv";
        }
    }
    
    /**
     * 데이터 파일이 존재하는지 확인합니다.
     */
    public boolean dataFileExists() {
        switch (currentFormat) {
            case CSV:
                return csvFileHandler.fileExists();
            case JSON:
                return jsonFileHandler.fileExists();
            default:
                return false;
        }
    }
    
    /**
     * 현재 파일 형식을 가져옵니다.
     */
    public FileFormat getCurrentFormat() {
        return currentFormat;
    }
    
    /**
     * 파일 형식을 변경합니다. 기존 데이터는 새 형식으로 저장됩니다.
     */
    public boolean changeFormat(FileFormat newFormat) {
        if (newFormat == currentFormat) {
            System.out.println("이미 " + newFormat.getDescription() + "을(를) 사용하고 있습니다.");
            return true;
        }
        
        FileFormat oldFormat = currentFormat;
        currentFormat = newFormat;
        
        // 새 형식으로 데이터 저장
        boolean success = saveData();
        if (success) {
            System.out.printf("파일 형식이 %s에서 %s(으)로 변경되었습니다.%n", 
                oldFormat.getDescription(), newFormat.getDescription());
        } else {
            // 실패 시 원래 형식으로 되돌리기
            currentFormat = oldFormat;
            System.err.println("파일 형식 변경에 실패했습니다.");
        }
        
        return success;
    }
    
    /**
     * 지원되는 모든 파일 형식을 가져옵니다.
     */
    public static FileFormat[] getSupportedFormats() {
        return FileFormat.values();
    }
    
    /**
     * 항목 목록을 형식화된 표로 표시합니다.
     */
    public void displayItems(List<LedgerItem> itemsToDisplay) {
        if (itemsToDisplay.isEmpty()) {
            System.out.println("표시할 항목이 없습니다.");
            return;
        }
        
        System.out.println("====================================================================================");
        System.out.printf(" %-3s | %-4s | %-12s | %-10s | %-11s | %-20s%n", 
            "ID", "유형", "날짜", "카테고리", "금액", "설명");
        System.out.println("------------------------------------------------------------------------------------");
        
        for (LedgerItem item : itemsToDisplay) {
            System.out.printf(" %-3d | %-4s | %-12s | %-10s | %-11d | %-20s%n",
                item.getId(),
                item.getType(),
                item.getDate(),
                item.getCategory(),
                item.getAmount(),
                item.getDescription() != null ? item.getDescription() : ""
            );
        }
        
        System.out.println("====================================================================================");
        System.out.printf("총 항목 수: %d%n", itemsToDisplay.size());
    }

}