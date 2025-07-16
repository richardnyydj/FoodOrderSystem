package com.ordersystem.model;

import java.time.LocalDateTime;

public class Order extends AbstractOrder {
  private LocalDateTime time; // 訂單建立時間

  public Order(long id) {
    super(id); // 呼叫父類別的建構子
    this.time = LocalDateTime.now(); // 記錄當前時間
  }

  // 取消訂單
  public void cancel() {
    this.status = OrderStatus.CANCELLED;
  }

  // 取得訂單時間
  public LocalDateTime getTime() {
    return this.time;
  }

  // 設定訂單時間
  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  // 檢查訂單是否過期，超過24小時
  public boolean isExpired() {
    return LocalDateTime.now().minusHours(24).isAfter(this.time);
  }

  // 是否為新的訂單，下訂單後的1小時
  public boolean isNewOrder() {
    return LocalDateTime.now().minusHours(1).isBefore(this.time);
  }

  // 處理訂單流程
  public void processOrder(Order order) {
    if(order.getStatus() == OrderStatus.WAITING) {
      // 檢查訂單時間
      if(!order.isExpired()) {
        order.setStatus(OrderStatus.PROCESSING);
      } else {
        order.cancel();
      }
    }
  }

  // 產生訂單報表
  public void orderReport(Order order) {
    String report = String.format("訂單編號：%d\n建立時間：%s\n狀態：%s\n總金額：%.2f",
      order.getId(),
      order.getTime(),
      order.getStatus(),
      order.getTotalPrice()
    );

    String.format("", "");
    System.out.println("訂單報表：" + report);
  }
}

/*
用途：實作實際可使用的訂單類別。
getOrderDetails：回傳菜單品項摘要字串。
calculateTotal：計算總金額。
此類簡潔地繼承與實作，完全實踐了物件導向的「開放封閉原則」。
 */