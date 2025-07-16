package com.ordersystem.controller;

import com.ordersystem.model.AbstractConsumer;
import com.ordersystem.model.AbstractOrder;
import com.ordersystem.model.Order;
import com.ordersystem.model.OrderStatus;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Consumer extends AbstractConsumer implements Runnable {
  private BlockingQueue<AbstractOrder> queue; // 訂單佇列
  private final Producer producer; // 參考生產者
  private volatile boolean processingEnabled = false; // 處理開關

  public Consumer(BlockingQueue<AbstractOrder> queue, Producer producer) {
    super();
    this.queue = queue;
    this.producer = producer;

    // 與生產者共享同一份訂單列表
    this.orders = producer.getTotalOrders();
  }

  // 執行緒的運行邏輯
  @Override
  public void run() {
    try {
      while(!Thread.currentThread().isInterrupted()) {
        // 從訂單佇列中取得新的訂單
        AbstractOrder order = this.queue.take();

        try {
          Order concreteOrder = (Order) order;

          // 如果訂單不在追蹤訂單列表中，則加入
          if(!this.orders.contains(concreteOrder)) {
            this.orders.add(concreteOrder);
          }

          // 等待直到處理被啟動
          while(!processingEnabled) {
            Thread.sleep(1000);
          }

          // 處理後重置標記
          processingEnabled = false;
        } catch(ClassCastException e) {
          System.err.println("錯誤的order型別" + e.getMessage());
        }
      }
    } catch(InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  // 啟動處理
  public void startProcessing() {
    this.processingEnabled = true;
  }

  // 處理特定訂單
  @Override
  public boolean processOrder(long id) {
    for(AbstractOrder order : this.orders) {
      if(order.getId() == id && order.getStatus() != OrderStatus.COMPLETED) {
        order.setStatus(OrderStatus.PROCESSING);
        System.out.println("訂單正在處理中..." + id);
        return true;
      }
    }

    return false;
  }

  // 處理下一個等待中的訂單
  public void processNextOrder() {
    for(AbstractOrder order : this.orders) {
      if(order.getStatus() == OrderStatus.WAITING) {
        boolean processed = processOrder(order.getId());
        if(processed) {
          break;
        }
      }
    }
  }

  // 取得正在處理的訂單
  public AbstractOrder getProcessingOrder() {
    for(AbstractOrder order : this.orders) {
      if(order.getStatus() == OrderStatus.PROCESSING) {
        return order;
      }
    }

    return null;
  }

  // 計算完成的訂單數量
  public int getCountOfCompletedOrders() {
    int count = 0;

    for(AbstractOrder order : this.orders) {
      if(order.getStatus() == OrderStatus.COMPLETED) {
        count++;
      }
    }

    return count;
  }

  public List<AbstractOrder> getTotalOrders() {
    return this.orders;
  }

  public void setProcessingOrder(Order order) {
    if(order != null) {
      order.setStatus(OrderStatus.PROCESSING);

      if(!orders.contains(order)) {
        orders.add(order);
      }
    }
  }

  public void addCompletedOrder(Order order) {
    if(order != null) {
      order.setStatus(OrderStatus.COMPLETED);

      if(!orders.contains(order)) {
        orders.add(order);
      }
    }
  }
}

/*
類似「後台廚房系統」：消化排隊中的訂單，更新訂單狀態。
執行緒會持續等待 queue 的新資料，達成非同步背景處理。
背景執行，模擬現實中處理訂單的過程。
 */