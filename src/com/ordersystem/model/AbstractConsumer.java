package com.ordersystem.model;

import java.util.ArrayList;
import java.util.List;

// 抽象消費者類別
public abstract class AbstractConsumer {
  protected List<AbstractOrder> orders; // 訂單列表

  // 建構子
  public AbstractConsumer() {
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

  // 處理訂單
  public boolean processOrder(long id) {
    for(AbstractOrder order : this.orders) {
      // 檢查是否有訂單正在處理中
      if(order.getStatus() == OrderStatus.PROCESSING) {
        return false;
      } else if(order.getId() == id) { // 找到指定訂單並開始處理
        order.setStatus(OrderStatus.PROCESSING);
        return true;
      }
    }

    return false;
  }

  // 完成訂單
  public void completedOrder(AbstractOrder order) {
    order.setStatus(OrderStatus.COMPLETED);
  }

  // 完成訂單的計數
  public int getCountOfCompletedOrders() {
    int count = 0;

    for(AbstractOrder order : this.orders) {
      if(order.getStatus() == OrderStatus.COMPLETED) {
        count++;
      }
    }

    return count;
  }

  // 取得已完成的訂單列表
  public List<AbstractOrder> getCompletedOrders() {
    List<AbstractOrder> completedList = new ArrayList<>();

    for(AbstractOrder order : this.orders) {
      if(order.getStatus() == OrderStatus.COMPLETED) {
        completedList.add(order);
      }
    }

    return completedList;
  }

  // 計算營收
  public double getRevenue() {
    double sum = 0;

    for(AbstractOrder order : this.orders) {
      if(order.getStatus() == OrderStatus.COMPLETED) {
        sum += order.getTotalPrice();
      }
    }

    return sum;
  }
}

/*
用途：模擬「消費者」（處理訂單）角色。
消費邏輯會從 queue 取出訂單並更新狀態。
通常由 Consumer.java 實作具體邏輯。
 */