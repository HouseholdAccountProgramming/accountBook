package com.accountbook.ui;

import com.accountbook.model.LedgerItem;
import com.accountbook.service.LedgerService;
import com.accountbook.util.CategoryManager;
import com.accountbook.util.FileFormat;
import com.accountbook.util.ValidationUtil;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

// 개인 가계부 애플리케이션을 위한 명령줄 인터페이스입니다.
public class CliInterface {

    // LedgerService 인스턴스는 유지합니다.
    private final LedgerService ledgerService;
    private boolean running;

    public CliInterface() {
        this.ledgerService = new LedgerService();
        this.running = true;
    }

    public CliInterface(String fileName) {
        this.ledgerService = new LedgerService(fileName);
        this.running = true;
    }

    // CLI 애플리케이션을 시작합니다.
    public void start() {
        System.out.println("개인 가계부에 오신 것을 환영합니다!");
        System.out.printf("데이터 파일: %s%n", ledgerService.getFileName());
        System.out.printf("기존 항목 %d개를 불러왔습니다.%n%n", ledgerService.getItemCount());

        // Scanner 객체는 try-with-resources를 사용하여 자동으로 자원 해제되도록 합니다.
        try (Scanner localScanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            while (running) {
                showMainMenu();
                handleMainMenuChoice(localScanner);
            }
        } 

        System.out.println("개인 가계부를 이용해 주셔서 감사합니다!");
    }

    // 메인 메뉴를 표시합니다.
    private void showMainMenu() {
        System.out.println("==== 개인 가계부 ====");
        System.out.printf("현재 파일 형식: %s%n", ledgerService.getCurrentFormat().getDescription());
        System.out.println("1. 내역 관리");
        System.out.println(" 1.1 내역 추가");
        System.out.println(" 1.2 내역 삭제");
        System.out.println(" 1.3 내역 수정");
        System.out.println("2. 내역 조회");
        System.out.println(" 2.1 전체 보기");
        System.out.println(" 2.2 날짜 범위별 보기");
        System.out.println(" 2.3 카테고리별 보기");
        System.out.println("3. 파일 불러오기");
        System.out.println("4. 파일 형식 변경");
        System.out.println("5. 프로그램 종료");
        System.out.println();
        System.out.print("옵션 선택: ");
    }

    // 메인 메뉴 선택을 처리합니다.
    private void handleMainMenuChoice(Scanner localScanner) {
        String input = localScanner.nextLine();
        ValidationUtil.ValidationResult result = ValidationUtil.validateMenuOption(input, 1, 5);

        if (!result.isValid()) {
            System.out.println("오류: " + result.getErrorMessage());
            System.out.println();
            return;
        }

        int choice = result.getValue(Integer.class);
        System.out.println();

        // Rule Switch 적용
        switch (choice) {
            case 1 -> handleManageItemsMenu(localScanner);
            case 2 -> handleViewItemsMenu(localScanner);
            case 3 -> loadFromFile(localScanner);
            case 4 -> changeFileFormat(localScanner);
            case 5 -> running = false;
        }
    }

    // 내역 관리 서브메뉴
    private void handleManageItemsMenu(Scanner localScanner) {
        System.out.println("=== 내역 관리 ===");
        System.out.println("1. 내역 추가");
        System.out.println("2. 내역 삭제");
        System.out.println("3. 내역 수정");
        System.out.print("옵션 선택: ");

        String input = localScanner.nextLine();
        ValidationUtil.ValidationResult result = ValidationUtil.validateMenuOption(input, 1, 3);

        if (!result.isValid()) {
            System.out.println("오류: " + result.getErrorMessage());
            System.out.println();
            return;
        }

        int choice = result.getValue(Integer.class);
        System.out.println();

        // Rule Switch 적용
        switch (choice) {
            case 1 -> addItem(localScanner);
            case 2 -> deleteItem(localScanner);
            case 3 -> editItem(localScanner);
        }
    }

    // 내역 조회 서브메뉴
    private void handleViewItemsMenu(Scanner localScanner) {
        System.out.println("=== 내역 조회 ===");
        System.out.println("1. 전체 보기");
        System.out.println("2. 날짜 범위별 보기");
        System.out.println("3. 카테고리별 보기");
        System.out.print("옵션 선택: ");

        String input = localScanner.nextLine();
        ValidationUtil.ValidationResult result = ValidationUtil.validateMenuOption(input, 1, 3);

        if (!result.isValid()) {
            System.out.println("오류: " + result.getErrorMessage());
            System.out.println();
            return;
        }

        int choice = result.getValue(Integer.class);
        System.out.println();

        // Rule Switch 적용
        switch (choice) {
            case 1 -> viewAllItems();
            case 2 -> viewItemsByDateRange(localScanner);
            case 3 -> viewItemsByCategory(localScanner);
        }
    }

    // 가계부에 새 항목을 추가합니다.
    private void addItem(Scanner localScanner) {
        System.out.println("=== 새 내역 추가 ===");

        String type;
        while (true) {
            System.out.print("유형 입력 [1: 수입 (+), 2: 지출 (-)]: ");
            String input = localScanner.nextLine();

            if ("cancel".equalsIgnoreCase(input.trim())) {
                if (confirmCancel(localScanner))
                    return;
                continue;
            }

            ValidationUtil.ValidationResult result = ValidationUtil.validateMenuOption(input, 1, 2);

            if (!result.isValid()) {
                System.out.println("오류: " + result.getErrorMessage());
                continue;
            }

            int choice = result.getValue(Integer.class);
            type = (choice == 1) ? "수입" : "지출";
            break;
        }

        // 날짜 입력 및 유효성 검사 (버그 수정 로직)
        LocalDate date = inputDateWithValidation("날짜 입력 (YYYY-MM-DD): ", localScanner);
        if (date == null)
            return;

        Integer amount = getValidAmount("금액 입력: ", localScanner);
        if (amount == null)
            return;

        String typeSymbol;
        if (type.equals("지출")) {
            amount = -Math.abs(amount);
            typeSymbol = " 지출 (-)";
        } else {
            amount = Math.abs(amount);
            typeSymbol = "수입 (+)";
        }

        // 카테고리 가져오기
        String category = promptCategoryWithManagement(null, localScanner);
        if (category == null) return;

        String description = getValidDescription("설명 입력 (선택 사항, 최대 50자 이내): ", localScanner);
        if (description == null)
            return;

        boolean success = ledgerService.addItem(typeSymbol, date, amount, category, description);
        if (!success) {
            System.out.println("오류: 파일에 항목을 저장하지 못했습니다.");
        } else {
            System.out.println("항목이 성공적으로 추가되었습니다.");
        }
        System.out.println();
    }

    // 내역 삭제
    private void deleteItem(Scanner localScanner) {
        System.out.println("=== 내역 삭제 ===");

        if (ledgerService.getItemCount() == 0) {
            System.out.println("삭제할 항목이 없습니다.");
            System.out.println();
            return;
        }

        System.out.println("현재 항목:");
        ledgerService.displayItems(ledgerService.getAllItems());
        System.out.println();

        System.out.print("삭제할 항목의 ID 입력: ");
        String input = localScanner.nextLine();

        if ("cancel".equalsIgnoreCase(input.trim())) {
            if (confirmCancel(localScanner))
                return;
        }

        try {
            int id = Integer.parseInt(input.trim());
            boolean deleted = ledgerService.deleteItem(id);
            if (deleted) {
                System.out.printf("ID %d 항목이 성공적으로 삭제되었습니다.%n", id);
            } else {
                System.out.println("오류: 해당 ID의 항목이 존재하지 않습니다.");
            }
        } catch (NumberFormatException e) {
            System.out.println("오류: 유효한 ID 번호를 입력해주세요.");
        }

        System.out.println();
    }

    // 내역 수정
    private void editItem(Scanner localScanner) {
        System.out.println("=== 내역 수정 ===");

        if (ledgerService.getItemCount() == 0) {
            System.out.println("수정할 항목이 없습니다.");
            System.out.println();
            return;
        }

        System.out.println("현재 항목:");
        ledgerService.displayItems(ledgerService.getAllItems());
        System.out.println();

        System.out.print("수정할 항목의 ID 입력: ");
        String idInput = localScanner.nextLine();

        if ("cancel".equalsIgnoreCase(idInput.trim())) {
            if (confirmCancel(localScanner))
                return;
        }

        try {
            int id = Integer.parseInt(idInput.trim());

            Optional<LedgerItem> itemOpt = ledgerService.getAllItems().stream()
                    .filter(item -> item.getId() == id)
                    .findFirst();

            if (itemOpt.isEmpty()) {
                System.out.println("오류: 해당 ID의 항목이 존재하지 않습니다.");
                System.out.println();
                return;
            }

            LedgerItem itemToEdit = itemOpt.get();

            System.out.println("--- 항목 수정 모드 ---");
            String currentType = itemToEdit.getAmount() >= 0 ? "수입" : "지출";
            System.out.printf("ID: %d%n", itemToEdit.getId());
            System.out.printf("1. 유형: %s%n", currentType);
            System.out.printf("2. 날짜: %s%n", itemToEdit.getDate());
            System.out.printf("3. 금액: %d%n", Math.abs(itemToEdit.getAmount()));
            System.out.printf("4. 카테고리: %s%n", itemToEdit.getCategory());
            System.out.printf("5. 내용: %s%n", itemToEdit.getDescription());
            System.out.println("---------------------");

            System.out.println("수정할 항목을 선택하세요:");
            System.out.println("1. 유형 (수입/지출) 2. 날짜 3. 금액 4. 카테고리 5. 내용");
            System.out.print("선택 > (취소: 'cancel' 입력): ");

            String fieldChoiceInput = localScanner.nextLine().trim();

            if ("cancel".equalsIgnoreCase(fieldChoiceInput)) {
                if (confirmCancel(localScanner))
                    return;
            }

            ValidationUtil.ValidationResult result = ValidationUtil.validateMenuOption(fieldChoiceInput, 1, 5);
            if (!result.isValid()) {
                System.out.println("오류: " + result.getErrorMessage());
                System.out.println();
                return;
            }

            int fieldChoice = result.getValue(Integer.class);
            Object newValue = null;

            // Rule Switch 적용
            switch (fieldChoice) {
                case 1 -> {
                    String newType = getValidType("유형 입력 [1: 수입 (+), 2: 지출 (-)]: ", localScanner);
                    if (newType == null)
                        return;
                    newValue = newType.equals("지출")
                            ? -Math.abs(itemToEdit.getAmount())
                            : Math.abs(itemToEdit.getAmount());
                }
                case 2 -> newValue = inputDateWithValidation("날짜 입력 (YYYY-MM-DD): ", localScanner);
                case 3 -> {
                    Integer newAmount = getValidAmount("금액 입력: ", localScanner);
                    if (newAmount == null)
                        return;
                    newValue = currentType.equals("지출") ? -Math.abs(newAmount) : Math.abs(newAmount);
                }
                case 4 -> {
                    System.out.println("현재 카테고리: " + itemToEdit.getCategory());
                    newValue = promptCategorySelection(localScanner);
                }
                case 5 -> newValue = getValidDescription("내용 입력 (선택사항, 최대 50자): ", localScanner);
            }

            if (newValue == null && fieldChoice != 1) {
                System.out.println("수정 작업이 취소되었습니다.");
                return;
            }

            updateItemToEdit(itemToEdit, fieldChoice, newValue);

            boolean success = ledgerService.saveData();

            if (success) {
                System.out.println("항목이 성공적으로 수정되었습니다.");
            } else {
                System.out.println("수정 작업이 취소되었거나 파일 저장에 실패했습니다.");
            }

        } catch (NumberFormatException e) {
            System.out.println("오류: 유효한 ID 번호를 입력해주세요.");
        }
        System.out.println();
    }

    // LedgerItem 객체에 변경 사항을 직접 반영
    private void updateItemToEdit(LedgerItem itemToEdit, int fieldChoice, Object newValue) {
        if (newValue == null)
            return;

        try {
            if (fieldChoice == 1 || fieldChoice == 3) {
                itemToEdit.setAmount((Integer) newValue);
                String newType = ((Integer) newValue >= 0) ? "수입 (+)" : " 지출 (-)";
                itemToEdit.setType(newType);
            } else {
                // Rule Switch 적용
                switch (fieldChoice) {
                    case 2 -> itemToEdit.setDate((LocalDate) newValue);
                    case 4 -> itemToEdit.setCategory((String) newValue);
                    case 5 -> itemToEdit.setDescription((String) newValue);
                }
            }
        } catch (Exception e) {
            System.out.println("오류: 데이터 모델 업데이트 중 예외 발생: " + e.getMessage());
        }
    }

    // 유형 입력
    private String getValidType(String prompt, Scanner localScanner) {
        while (true) {
            System.out.print(prompt + "(취소: 'cancel' 입력): ");
            String input = localScanner.nextLine();

            if ("cancel".equalsIgnoreCase(input.trim())) {
                if (confirmCancel(localScanner))
                    return null;
                continue;
            }

            ValidationUtil.ValidationResult result = ValidationUtil.validateMenuOption(input, 1, 2);

            if (result.isValid()) {
                int choice = result.getValue(Integer.class);
                return choice == 1 ? "수입" : "지출";
            } else {
                System.out.println("오류: " + result.getErrorMessage());
            }
        }
    }

    // 전체 보기
    private void viewAllItems() {
        System.out.println("=== 전체 내역 ===");
        List<LedgerItem> items = ledgerService.getAllItems();
        ledgerService.displayItems(items);
        System.out.println();
    }

    // 날짜 범위별 보기
    private void viewItemsByDateRange(Scanner localScanner) {
        System.out.println("=== 날짜 범위별 보기 ===");

        LocalDate startDate = inputDateWithValidation("시작 날짜 입력 (YYYY-MM-DD): ", localScanner);
        if (startDate == null)
            return;

        LocalDate endDate = inputDateWithValidation("종료 날짜 입력 (YYYY-MM-DD): ", localScanner);
        if (endDate == null)
            return;

        if (startDate.isAfter(endDate)) {
            System.out.println("오류: 시작 날짜가 종료 날짜보다 뒤일 수 없습니다.");
            System.out.println();
            return;
        }

        List<LedgerItem> items = ledgerService.getItemsByDateRange(startDate, endDate);
        System.out.printf("%s부터 %s까지의 항목:%n", startDate, endDate);
        ledgerService.displayItems(items);
        System.out.println();
    }

    // 카테고리별 보기
    private void viewItemsByCategory(Scanner localScanner) {
        System.out.println("=== 카테고리별 보기 ===");

        String category = promptCategorySelection(localScanner);
        if (category == null) return;
        

        List<LedgerItem> items = ledgerService.getItemsByCategory(category);
        System.out.printf("'%s' 카테고리의 항목:%n", category);
        ledgerService.displayItems(items);
        System.out.println();
    }

    // 파일 불러오기
    private void loadFromFile(Scanner localScanner) {
        System.out.println("=== 파일 불러오기 ===");
        System.out.print("현재 데이터가 덮어씌워집니다. 계속하시겠습니까? (Y/N): ");
        String confirm = localScanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            boolean success = ledgerService.loadData(true);
            if (!success) {
                System.out.println("파일에서 데이터를 불러오지 못했습니다.");
            }
        } else {
            System.out.println("불러오기가 취소되었습니다.");
        }
        System.out.println();
    }

    // 파일 형식 변경
    private void changeFileFormat(Scanner localScanner) {
        System.out.println("=== 파일 형식 변경 ===");
        System.out.printf("현재 형식: %s%n", ledgerService.getCurrentFormat().getDescription());
        System.out.println();

        FileFormat[] formats = LedgerService.getSupportedFormats();
        System.out.println("지원되는 파일 형식:");
        for (int i = 0; i < formats.length; i++) {
            String current = formats[i] == ledgerService.getCurrentFormat() ? " (현재)" : "";
            System.out.printf("%d. %s%s%n", i + 1, formats[i].getDescription(), current);
        }
        System.out.println();

        System.out.print("새 파일 형식을 선택하세요 (1-" + formats.length + "): ");
        String input = localScanner.nextLine();

        ValidationUtil.ValidationResult result = ValidationUtil.validateMenuOption(input, 1, formats.length);
        if (!result.isValid()) {
            System.out.println("오류: " + result.getErrorMessage());
            System.out.println();
            return;
        }

        int choice = result.getValue(Integer.class);
        FileFormat selectedFormat = formats[choice - 1];

        if (selectedFormat == ledgerService.getCurrentFormat()) {
            System.out.println("이미 선택된 형식입니다.");
        } else {
			System.out.printf("%s(으)로 형식을 변경하시겠습니까? (Y/N): ", selectedFormat.getDescription());
            String confirm = localScanner.nextLine().trim().toLowerCase();

            if (confirm.equals("y") || confirm.equals("yes")) {
                boolean success = ledgerService.changeFormat(selectedFormat);
                if (!success) {
                    System.out.println("파일 형식 변경에 실패했습니다.");
                }
            } else {
                System.out.println("형식 변경이 취소되었습니다.");
            }
        }
        System.out.println();
    }

    // 취소 확인
    private boolean confirmCancel(Scanner localScanner) {
        System.out.print("# 확인: 현재 작업을 취소하고 메인 화면으로 이동하시겠습니까? (Y/N) > ");
        String confirm = localScanner.nextLine().trim().toLowerCase();

        if ("y".equals(confirm)) {
            System.out.println("작업이 취소되었습니다. 메인 메뉴로 돌아갑니다.");
            return true;
        } else {
            System.out.println("작업을 재개합니다.");
            return false;
        }
    }

    // ====== 여기부터 입력 유효성 헬퍼들 ======

    /**
     * 날짜 입력을 받고 유효성 검사가 통과할 때까지 반복합니다.
     * @param prompt 사용자에게 표시할 메시지
     * @return 유효한 LocalDate 객체, 취소 시 null
     */
    private LocalDate inputDateWithValidation(String prompt, Scanner localScanner) {
        while (true) {
            System.out.print(prompt + "(취소: 'cancel' 입력): ");
            String input = localScanner.nextLine();
            
            // [DEBUG] 로그 제거 (이제 필요 없음)

            if ("cancel".equalsIgnoreCase(input.trim())) {
                if (confirmCancel(localScanner))
                    return null;
                continue;
            }

            ValidationUtil.ValidationResult result = ValidationUtil.validateDate(input);
            if (result.isValid()) {
                return result.getValue(LocalDate.class);
            } else {
                // [수정]: 오류 메시지 출력 후 System.out.flush()를 호출하여 즉시 출력하도록 합니다.
                System.out.println("오류: " + result.getErrorMessage());
                System.out.flush(); // 버퍼링된 출력을 강제 전송
            }
        }
    }

    private Integer getValidAmount(String prompt, Scanner localScanner) {
        while (true) {
            System.out.print(prompt + "(취소: 'cancel' 입력): ");
            String input = localScanner.nextLine();

            if ("cancel".equalsIgnoreCase(input.trim())) {
                if (confirmCancel(localScanner))
                    return null;
                continue;
            }

            ValidationUtil.ValidationResult result = ValidationUtil.validateAmount(input);
            if (result.isValid()) {
                return result.getValue(Integer.class);
            } else {
                System.out.println("오류: " + result.getErrorMessage());
            }
        }
    }
    
    // getValidCategory는 사용되지 않아 제거되었습니다. (CategoryManager에서 처리)
    
    private String getValidDescription(String prompt, Scanner localScanner) {
        while (true) {
            System.out.print(prompt + "(취소: 'cancel' 입력): ");
            String input = localScanner.nextLine();

            if ("cancel".equalsIgnoreCase(input.trim())) {
                if (confirmCancel(localScanner))
                    return null;
                continue;
            }

            ValidationUtil.ValidationResult result = ValidationUtil.validateDescription(input);
            if (result.isValid()) {
                return result.getValue(String.class);
            } else {
                System.out.println("오류: " + result.getErrorMessage());
            }
        }
    }


    // 카테고리 전체 목록을 번호와 함께 출력합니다.
    private void printCategoryList() {
        List<String> categories = CategoryManager.getAllCategories();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(i + 1).append(". ").append(categories.get(i));
        }
        sb.append("]");
        System.out.println("현재 카테고리: " + sb.toString());
    }

    // 2.1 내역 추가 - 카테고리 입력: 번호 선택 또는 추가(Y)/삭제(N) 관리 포함
    private String promptCategoryWithManagement(String currentLabel, Scanner localScanner) {
        while (true) {
            if (currentLabel != null && !currentLabel.isEmpty()) {
                System.out.println("현재 값: " + currentLabel);
            }
            printCategoryList();
            System.out.print("카테고리 입력: [번호 선택 / 추가 Y / 삭제 N] (취소: 'cancel' 입력): ");
            String input = localScanner.nextLine().trim();

            if ("cancel".equalsIgnoreCase(input)) {
                if (confirmCancel(localScanner)) return null;
                continue;
            }

            // 번호 선택
            ValidationUtil.ValidationResult num = ValidationUtil.validateMenuOption(input, 1, CategoryManager.getAllCategories().size());
            if (num.isValid()) {
                int idx = num.getValue(Integer.class) - 1;
                return CategoryManager.getAllCategories().get(idx);
            }

            // 추가
            if (input.equalsIgnoreCase("Y")) {
                System.out.print("카테고리 명을 입력해주세요: ");
                String name = localScanner.nextLine();
                if ("cancel".equalsIgnoreCase(name.trim())) {
                    if (confirmCancel(localScanner)) return null;
                    continue;
                }
                CategoryManager.AddResult res = CategoryManager.addCustomCategory(name);
                if (!res.success) {
                    System.out.println(res.message);
                } else {
                    System.out.println("정상적으로 카테고리가 추가되었습니다.");
                }
                // 업데이트된 목록을 보여주고 다시 프롬프트로 복귀
                continue;
            }

            // 삭제
            if (input.equalsIgnoreCase("N")) {
                System.out.print("카테고리 명을 입력해주세요: ");
                String name = localScanner.nextLine();
                if ("cancel".equalsIgnoreCase(name.trim())) {
                    if (confirmCancel(localScanner)) return null;
                    continue;
                }
                CategoryManager.DeleteResult res = CategoryManager.deleteCustomCategory(name);
                if (!res.success) {
                    System.out.println(res.message);
                } else {
                    System.out.println("해당 카테고리 항목을 삭제하였습니다.");
                }
                // 자동 리넘버링은 리스트에서 자연스럽게 반영됨. 다시 프롬프트로 복귀
                continue;
            }

            System.out.println("오류: 유효한 번호, 'Y', 'N' 중 하나를 입력해주세요.");
        }
    }

    // 번호만으로 카테고리 선택 (수정, 카테고리별 보기에서 사용)
    private String promptCategorySelection(Scanner localScanner) {
        while (true) {
            printCategoryList();
            System.out.print("카테고리 번호를 입력해주세요 (취소: 'cancel' 입력): ");
            String input = localScanner.nextLine().trim();
            if ("cancel".equalsIgnoreCase(input)) {
                if (confirmCancel(localScanner)) return null;
                continue;
            }
            ValidationUtil.ValidationResult num = ValidationUtil.validateMenuOption(input, 1, CategoryManager.getAllCategories().size());
            if (num.isValid()) {
                int idx = num.getValue(Integer.class) - 1;
                return CategoryManager.getAllCategories().get(idx);
            }
            System.out.println("오류: 유효한 번호를 입력해주세요.");
        }
    }
}
