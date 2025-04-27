package org.allmart.productservice.exception;

public enum ProductErrorCode {
    PRODUCT_NOT_FOUND("상품을 찾을 수 없습니다."),
    PRODUCT_ALREADY_EXISTS("이미 존재하는 상품입니다."),
    PRODUCT_NAME_EMPTY("상품명이 비어있습니다."),
    STOCK_SHORTAGE("재고가 부족합니다."),
    INVALID_STOCK_QUANTITY("재고 수량이 0보다 작을 수 없습니다.");

    private final String message;

    ProductErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}