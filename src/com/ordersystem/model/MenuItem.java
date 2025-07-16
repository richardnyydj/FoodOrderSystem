package com.ordersystem.model;

public class MenuItem {
  private String name; // 餐點名稱
  private double price; // 餐點價格
  private String description; // 餐點描述

  public MenuItem(String name, double price, String description) {
    this.name = name;
    this.price = price;
    this.description = description;
  }

  public String getName() {
    return this.name;
  }

  public double getPrice() {
    return this.price;
  }

  @Override
  public String toString() {
    return String.format("%s-%.1f", getName(), getPrice());
  }
}

/*
用途：餐點資料模型，儲存菜名與價格。
使用場景：被 UI 中的菜單列、點餐紀錄等引用。
延伸：若未來有圖片或分類，也可擴充此類別。
 */