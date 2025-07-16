package com.ordersystem.model;

import java.util.Map;

public interface InterfaceOrder {
  long getId();
  Map<MenuItem, Integer> getItems();
  Map.Entry<MenuItem, Integer> getItem(String name);
  OrderStatus getStatus();
  void setStatus(OrderStatus status);
  void addItem(MenuItem item, int quantity);
  double getTotalPrice();
}

/*
用途：定義訂單應有的功能。
使用場景：Order 與 AbstractOrder 都要實作這個介面，符合「策略模式」概念。
優點：若未來擴充不同類型訂單（如外帶、內用），皆可統一處理。
 */