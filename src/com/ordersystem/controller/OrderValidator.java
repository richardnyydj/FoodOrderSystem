package com.ordersystem.controller;

import java.util.regex.Pattern;

public class OrderValidator {
  // 餐點數量 1~99
  private static final Pattern QUANTITY_PATTERN = Pattern.compile("^[1-9][0-9]?$");
  private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^\\d{17}$");
  // 允許整數或最多2位數
  private static final Pattern PRICE_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

  // 驗證數量
  public static boolean isValidQuantity(String quantity) {
    return quantity == null ? false : QUANTITY_PATTERN.matcher(quantity).matches();

    // if(quantity == null) {
    //   return false;
    // } else {
    //   return QUANTITY_PATTERN.matcher(quantity).matches();
    // }
  }

  // 驗證訂單ID
  public static boolean isValidOrderID(String orderId) {
    return orderId == null ? false : ORDER_ID_PATTERN.matcher(orderId).matches();
  }

  // 驗證價格
  public static boolean isValidPrice(String price) {
    return price == null ? false : PRICE_PATTERN.matcher(price).matches();
  }
}

/*
確保傳入的訂單不為空，且有點選品項。
通常會在 Producer 或 UI 傳入 Controller 前先執行這項驗證。
為單一職責原則的好例子，可單元測試。
 */