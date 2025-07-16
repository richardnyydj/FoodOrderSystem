package com.ordersystem.controller;

import com.ordersystem.model.MenuItem;
import com.ordersystem.model.Order;
import com.ordersystem.model.OrderStatus;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class OrderFileManager {
  // 定義訂單檔案的欄位名稱
  private enum OrderColumnName {
    ID("Order ID:"),
    TIME("Order Time:"),
    STATUS("Order Status"),
    ITEM("Menu Items:");

    private final String columnName;

    OrderColumnName(String columnName) {
      this.columnName = columnName;
    }

    public String getColumnName() {
      return this.columnName;
    }
  };

  private final String BASE_DIRECTORY = "orders"; // 儲存檔案的資料夾
  private final ReadWriteLock lock = new ReentrantReadWriteLock(); // 讀寫鎖

  public OrderFileManager() {
    File directory = new File(BASE_DIRECTORY);

    if(!directory.exists()) {
      directory.mkdirs(); // 如果資料夾不存在的話就建立
    }
  }

  // 儲存訂單方法
  public void saveOrders(List<Order> orders) {
    // 產生檔案名稱，格式：orders-年-月-日-時-分-秒.txt
    String filename = "orders-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".txt";

    File file = new File(BASE_DIRECTORY, filename);
    lock.writeLock().lock(); // 取得寫入鎖

    try(PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
      for(Order order : orders) {
        writer.println("==== Orders Details ====");
        writer.println(OrderColumnName.ID.getColumnName() + " " + order.getId());
        writer.println(OrderColumnName.TIME.getColumnName() + " " + order.getTime());
        writer.println(OrderColumnName.STATUS.getColumnName() + " " + order.getStatus());

        writer.println(OrderColumnName.ITEM.getColumnName());
        for(Map.Entry<MenuItem, Integer> item : order.getItems().entrySet()) {
          writer.printf("%s-%.1f-%d%n",
            item.getKey().getName(),
            item.getKey().getPrice(),
            item.getValue()
          );
        }
        writer.println("========================");
      }
    } catch(IOException e) {
      e.printStackTrace();
    } finally {
      lock.writeLock().unlock();
    }
  }

  // 讀取訂單方法
  public List<Order> readAllOrders() {
    // 檢查目錄
    File directory = new File(BASE_DIRECTORY);
    if(!directory.exists() || !directory.isDirectory()) {
      return new ArrayList<>();
    }

    // 取得所有訂單檔案
    File[] ordersFiles = directory.listFiles((dir, name) -> name.startsWith("orders-") && name.endsWith(".txt"));

    // 排序檔案 (最新的在前) 降冪排序
    Arrays.sort(ordersFiles, (file1, file2) -> file2.getName().compareTo(file1.getName()));

    // 讀取最新的檔案
    File mostRecentFile = ordersFiles[0];

    List<Order> allOrders = new ArrayList<>();

    lock.readLock().lock(); // 取得讀取鎖

    // 讀取檔案內容
    try(BufferedReader reader = new BufferedReader(new FileReader(mostRecentFile))) {
      // 暫存當前讀取的訂單資訊
      String line;
      long orderId = 0;
      LocalDateTime time = null;
      String status = null;
      Map<MenuItem, Integer> items = new HashMap<>();

      while((line = reader.readLine()) != null) {
        // 開始新訂單
        if(line.startsWith("==== Orders Details ====")) {
          // 重置所有變數
          orderId = 0;
          time = null;
          status = null;
          items = new HashMap<>();
        }
        // 讀取訂單ID
        else if(line.startsWith(OrderColumnName.ID.getColumnName())) {
          String orderIdString = line.substring(OrderColumnName.ID.getColumnName().length()).trim();
          orderId = Long.parseLong(orderIdString);
        }
        // 讀取訂單時間
        else if(line.startsWith(OrderColumnName.TIME.getColumnName())) {
          String timeString = line.substring(OrderColumnName.TIME.getColumnName().length()).trim();
          time = LocalDateTime.parse(timeString);
        }
        // 讀取訂單狀態
        else if(line.startsWith(OrderColumnName.STATUS.getColumnName())) {
          status = line.substring(OrderColumnName.STATUS.getColumnName().length()).trim();
        }
        // 讀取餐點項目
        else if(line.contains("-")) {
          String[] parts = line.split("-");
          if(parts.length == 3) {
            String name = parts[0];
            double price = Double.parseDouble(parts[1]);
            int quantity = Integer.parseInt(parts[2]);
            MenuItem menuItem = new MenuItem(name, price, "");
            items.put(menuItem, quantity);
          }
        }

        // 訂單結束，建立訂單物件
        else if(line.equals("========================") && orderId != 0) {
          Order order = new Order(orderId);
          order.setTime(time);
          order.setStatus(OrderStatus.valueOf(status));
          for(Map.Entry<MenuItem, Integer> entry : items.entrySet()) {
            order.addItem(entry.getKey(), entry.getValue());
          }
          allOrders.add(order);
        }
      }

    } catch(IOException e) {
      e.printStackTrace();
    } finally {
      lock.readLock().unlock();
    }

    return allOrders;
  }
}

/*
將訂單寫入檔案
與 Model 層的 Order 搭配，將訂單資料儲存為文字。
是 IO 相關的邏輯，抽離出來符合 SRP 原則。
 */