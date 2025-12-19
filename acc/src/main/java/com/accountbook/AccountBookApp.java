package com.accountbook;

import com.accountbook.ui.CliInterface;

/**
 * Personal Account Book CLI 프로그램의 메인 애플리케이션 클래스입니다.
 * * 이 애플리케이션은 사용자에게 다음 기능을 제공합니다:
 * - 금융 가계부 항목 추가, 삭제 및 조회
 * - CSV 파일 영속성을 통한 데이터 관리
 * - 직관적인 메뉴 기반 인터페이스를 통한 탐색
 * * 사용법: java com.accountbook.AccountBookApp [파일명]
 * 파일명이 제공되지 않으면 기본값인 "ledger.csv"가 사용됩니다.
 */
public class AccountBookApp {
    
    public static void main(String[] args) {
        try {
            CliInterface cli;
            
            // 사용자 지정 파일명이 제공되었는지 확인
            if (args.length > 0) {
                String fileName = args[0];
                System.out.println("사용자 지정 데이터 파일: " + fileName + "을(를) 사용합니다.");
                cli = new CliInterface(fileName);
            } else {
                cli = new CliInterface();
            }
            
            // 애플리케이션 시작
            cli.start();
            
        } catch (Exception e) {
            System.err.println("예상치 못한 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}