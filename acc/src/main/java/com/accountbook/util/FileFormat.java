package com.accountbook.util;

/**
 * 지원되는 파일 형식을 정의하는 열거형입니다.
 */
public enum FileFormat {
    CSV("csv", "CSV 형식"),
    JSON("json", "JSON 형식");
    
    private final String extension;
    private final String description;
    
    FileFormat(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 파일 확장자로부터 FileFormat을 찾습니다.
     */
    public static FileFormat fromExtension(String extension) {
        for (FileFormat format : values()) {
            if (format.extension.equalsIgnoreCase(extension)) {
                return format;
            }
        }
        return null;
    }
    
    /**
     * 파일명으로부터 FileFormat을 추론합니다.
     */
    public static FileFormat fromFileName(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return fromExtension(extension);
    }
}