package com.ordersystem.view;

public enum Food {
  BURGER("漢堡", "burger.jpg", 50),
  FRIES("薯條", "fries.jpg", 20),
  COFFEE("咖啡", "coffee.jpg", 40),
  COLA("可樂", "cola.jpg", 25);

  private final String name;
  private final String filename;
  private final double price;

  Food(String name, String filename, double price) {
    this.name = name;
    this.filename = filename;
    this.price = price;
  }

  public String getName() {
    return this.name;
  }

  public String getFileName() {
    return this.filename;
  }

  public double getPrice() {
    return this.price;
  }
}

/*
這是一個客製化的 UI 元件（類似小卡片）
使用 Consumer<MenuItem> 來處理點餐邏輯（很現代的設計）
與 Model 中的 MenuItem 結合
呼叫時將事件傳遞給外部 UI（如 OrderSystemUI）
 */