package com.ordersystem.model;

import java.util.ArrayList;
import java.util.List;

// 抽象生產者類別
public abstract class AbstractProducer {
  protected final List<AbstractOrder> orders;

  // 建構子
  public AbstractProducer() {
    this.orders = new ArrayList<>();
  }

  // 新增訂單
  public void addOrder(AbstractOrder order) {
    this.orders.add(order);
  }

  // 取得所有訂單
  public List<AbstractOrder> getTotalOrders() {
    return this.orders;
  }

  // 訂單取消功能
  public boolean cancelOrder(long id) {
    for(AbstractOrder abstractOrder : this.orders) {
      // 找到指定訂單
      if(abstractOrder.getId() == id) {
        // 轉型為具體訂單
        Order order = (Order) abstractOrder;
        // 取消訂單
        order.cancel();
        // 取消成功
        return true;
      }
    }

    // 找不到訂單
    return false;
  }

  // 計算特定訂單總價
  public double getOrderTotalPrice(long id) {
    for(AbstractOrder order : this.orders) {
      if(order.getId() == id) {
        // 回傳指定訂單的總價
        return order.getTotalPrice();
      }
    }

    // 找不到訂單時回傳0
    return 0;
  }

  // 計算所有訂單總價
  public double getTotalPrice() {
    double sum = 0;

    for(AbstractOrder order : this.orders) {
      sum += order.getTotalPrice();
    }

    return sum;
  }
}

/*
用途：模擬「生產者」（下訂單）角色，將訂單加入處理佇列。
BlockingQueue：執行緒安全佇列，適用多執行緒環境。
為什麼繼承 Thread：讓 Producer 可以在背景運行，模擬實際餐廳場景。
 */