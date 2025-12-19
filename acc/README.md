📖 개인 가계부 - CLI 애플리케이션
개인 재정을 관리하기 위한 자바 기반의 명령줄 인터페이스(CLI) 애플리케이션입니다.

필수 요구사항
- Java JDK 17 이상 (권장: JDK 21 LTS)
- Windows 환경(PowerShell) 또는 기타 JVM 지원 OS
- 외부 종속성 없음

빠른 실행
- 컴파일: `javac -encoding UTF-8 -d . src/main/java/com/accountbook/*.java src/main/java/com/accountbook/model/*.java src/main/java/com/accountbook/service/*.java src/main/java/com/accountbook/ui/*.java src/main/java/com/accountbook/util/*.java`
- 실행(기본 파일 사용): `java com.accountbook.AccountBookApp`
- 실행(사용자 지정 파일): `java com.accountbook.AccountBookApp my_ledger.csv`

📝 주요 기능
- 거래 내역 관리(추가/삭제/수정)
- 데이터 조회(전체/날짜 범위/카테고리별)
- CSV/JSON 파일 형식 저장/불러오기 및 변경

기능 상세
- 추가: 유형(수입/지출), 날짜(YYYY-MM-DD), 금액(1원~1억 원), 카테고리, 내용(최대 50자)
- 삭제: 항목별 고유 ID 기준으로 제거
- 수정: ID 기준으로 날짜/금액/카테고리/내용을 편집하고 즉시 저장
- 전체 보기: 메모리에 저장된 모든 항목 출력
- 날짜 범위별 보기: 시작/종료일로 필터링
- 카테고리별 보기: 기본 6개 + 사용자 지정 최대 4개를 번호로 선택해 조회

파일 처리 및 영속성
- CSV/JSON 형식 지원 및 상호 변환
- 항목 추가/삭제/수정 시 메모리와 파일에 즉시 동기화
- 파일 로드 및 입력 시 형식/범위/유효성 엄격 검증

환경 및 사용자 편의성
- 메뉴 기반 인터페이스(번호 입력)
- 작업 취소 기능: 모든 입력 단계에서 `cancel` 입력 시 메인 메뉴로 복귀
- 반복 실행: 메인 메뉴 ↔ 기능 수행 반복 구조

📂 프로젝트 구조
애플리케이션은 model, service, ui, util 패키지로 분리되어 유지보수성을 높였습니다.
└── main/
    └── java/
        └── com/
            └── accountbook/
                ├── AccountBookApp.java        
                ├── model/
                │   └── LedgerItem.java        
                ├── service/
                │   └── LedgerService.java     
                ├── ui/
                │   └── CliInterface.java      
                └── util/
                    ├── ValidationUtil.java    
                    ├── FileFormat.java        
                    ├── CsvFileHandler.java    
                    └── JsonFileHandler.java   
