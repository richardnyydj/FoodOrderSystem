package com.ordersystem.model;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractOrder implements InterfaceOrder {
  protected final long id; // 訂單編號
  protected OrderStatus status; // 訂單狀態
  protected Map<MenuItem, Integer> items; // 訂單項目

  public AbstractOrder(long id) {
    this.id = id; // 設定訂單編號
    this.items = new HashMap<>(); // 初始化訂單項目Map
    this.status = OrderStatus.WAITING; // 設定初始狀態為等待中
  }

  @Override
  public long getId() {
    return this.id;
  }

  @Override
  public Map<MenuItem, Integer> getItems() {
    return this.items;
  }

  @Override
  public Map.Entry<MenuItem, Integer> getItem(String name) {
    // 根據品相名稱來查找訂單項目
    for(Map.Entry<MenuItem, Integer> item : this.items.entrySet()) {
      if(item.getKey().getName().equals(name)) {
        return item;
      }
    }

    return null;
  }

  @Override
  public OrderStatus getStatus() {
    return this.status;
  }

  @Override
  public void setStatus(OrderStatus status) {
    this.status = status;
  }

  @Override
  public void addItem(MenuItem item, int quantity) {
    // 如果餐點項目已經存在，則數量相加；如果不存在的話，創建新的餐點項目
    this.items.put(item, this.items.getOrDefault(item, 0) + quantity);
  }

  @Override
  public double getTotalPrice() {
    double sum = 0;

    // 計算所有餐點項目的總金額：餐點單價 ＊ 數量
    for(Map.Entry<MenuItem, Integer> item : this.items.entrySet()) {
      sum += item.getKey().getPrice() * item.getValue();
    }

    return sum;
  }
}

/*
用途：訂單的抽象基底，包含通用邏輯如：加入餐點、設定狀態。
與 InterfaceOrder 的關係：實作其方法並預留擴充彈性。
抽象類別設計好處：避免重複程式碼，提升可維護性。
 */