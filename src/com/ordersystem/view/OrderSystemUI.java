package com.ordersystem.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout; // Java AWT(Abstract Window Toolkit)
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ordersystem.controller.Consumer;
import com.ordersystem.controller.OrderFactory;
import com.ordersystem.controller.OrderFileManager;
import com.ordersystem.controller.Producer;
import com.ordersystem.model.AbstractOrder;
import com.ordersystem.model.MenuItem;
import com.ordersystem.model.Order;
import com.ordersystem.model.OrderStatus;

public class OrderSystemUI extends JFrame {
  // 定義UI元件
  private JPanel menuPanel; // 顯示菜單
  private JPanel orderPanel; // 點餐區域
  private JPanel cartListPanel; // 購物車清單
  private JPanel statusPanel; // 訂單狀態
  private JPanel statusDisplay; // 狀態顯示區

  // 定義狀態標籤
  private JLabel waitingStatusLabel; // 等待中訂單
  private JLabel processingStatusLabel; // 處理中訂單
  private JLabel completedStatusLabel; // 完成訂單
  private JLabel waitingTimeLabel; // 等待時間
  private JLabel processingTimeLabel; // 處理時間
  private JLabel completedTimeLabel; // 完成時間

  // 定義操作元件
  private JComboBox<MenuItem> itemSelector; // 餐點選擇下拉選單
  private JSpinner quantitySpinner; // 數量選擇器
  private JButton orderButton; // 送出訂單按鈕
  private JButton processButton; // 處理訂單按鈕
  private JButton addButton; // 加入購物車按鈕

  // 業務邏輯相關
  private Producer producer; // 訂單生產者
  private Consumer consumer; // 訂單消費者
  private Map<MenuItem, Integer> cartListItems; // 購物車項目
  private final List<MenuItem> menuItems = new ArrayList<>(); // 菜單項目
  private OrderSummaryPanel orderSummaryPanel; // 訂單摘要面板
  private OrderFileManager orderFileManager; // 訂單檔案管理器

  private static final int MAX_IMAGE_SIZE = 100;

  public OrderSystemUI(Producer producer, Consumer consumer) {
    this.producer = producer; //初始化
    this.consumer = consumer; //初始化
    this.cartListItems = new HashMap<>();
    this.orderFileManager = new OrderFileManager();

    // 初始化視窗和元件
    initializeFrame();
    createJComponents();
    addListeners(); //事件(點擊、滾動等)處理器
    loadSavedOrders(); // 載入已存儲的訂單
  }

  // 視窗初始化
  private void initializeFrame() {
    setTitle("餐廳點餐系統"); //視窗頂端名稱
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLayout(new BorderLayout());

    // 關閉視窗時的處理
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        try {
          // 儲存等待中和已完成的訂單
          List<Order> ordersToSave = new ArrayList<>();

          // 收集等待中的訂單
          for(AbstractOrder order : producer.getTotalOrders()) {
            if(order.getStatus() == OrderStatus.WAITING) {
              ordersToSave.add((Order) order);
            }
          }

          // 收集處理中的訂單
          AbstractOrder processingOrder = consumer.getProcessingOrder();
          if(processingOrder != null && processingOrder.getStatus() == OrderStatus.PROCESSING) {
            processingOrder.setStatus(OrderStatus.PROCESSING);
            ordersToSave.add((Order) processingOrder);
          }

          // 收集已完成的訂單
          for(AbstractOrder order : consumer.getCompletedOrders()) {
            if(order.getStatus() == OrderStatus.COMPLETED) {
              ordersToSave.add((Order) order);
            }
          }

          // 儲存訂單
          orderFileManager.saveOrders(ordersToSave);

        } catch(Exception ex) {
          System.err.println("Error saving orders: " + ex.getMessage());
        }
      }
    });
  }

  // 建立主要元件
  private void createJComponents() {
    // 建立主要面板
    this.menuPanel = createMenuPanel();
    this.orderPanel = createOrderPanel();
    this.statusPanel = createStatusPanel();

    this.orderSummaryPanel = new OrderSummaryPanel();

    this.menuPanel.setBackground(java.awt.Color.GRAY);

    add(this.menuPanel, BorderLayout.NORTH);
    add(this.orderPanel, BorderLayout.CENTER);
    add(this.statusPanel, BorderLayout.SOUTH);
    add(this.orderSummaryPanel, BorderLayout.EAST);
  }

  // 建立菜單面板
  private JPanel createMenuPanel() {
    JPanel panel = new JPanel(new GridLayout(1, Food.values().length, 15, 0));
    TitledBorder titledBorder = BorderFactory.createTitledBorder("菜單");
    titledBorder.setTitleColor(Color.WHITE);
    panel.setBorder(titledBorder);

    // 為每個餐點建立卡片
    for(Food food : Food.values()) {
      JPanel menuCardPanel = createMenuCardPanel(food.getName(), food.getFileName(), food.getPrice());

      if(menuCardPanel != null) {
        panel.add(menuCardPanel);
      }
    }

    return panel;
  }

  private JPanel createMenuCardPanel(String name, String filename, double price) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    // 建立圖片面板
    JPanel imageItem = new JPanel(new BorderLayout());
    imageItem.setBorder(BorderFactory.createTitledBorder(name));

    try {
      // 載入並且縮放圖片
      String imagePath = "src/images/" + filename;
      File imageFile = new File(imagePath);

      if(!imageFile.exists()) {
        System.err.println("圖片文件不存在：" + imagePath);
        return null;
      }

      BufferedImage imageBuffer = ImageIO.read(imageFile);

      // 縮小原始菜單的圖片
      int originalWidth = imageBuffer.getWidth(null);
      int originalHeight = imageBuffer.getHeight(null);

      double scale = Math.min((double) MAX_IMAGE_SIZE / originalWidth, (double) MAX_IMAGE_SIZE / originalHeight);
      int scaledWidth = (int) (originalWidth * scale);
      int scaledHeight = (int) (originalHeight * scale);

      Image scaleImage = imageBuffer.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

      JLabel imageLabel = new JLabel(new ImageIcon(scaleImage));
      imageItem.add(imageLabel);
    } catch(IOException e) {
      e.printStackTrace();
    }

    // 產生菜單價格
    JLabel priceLabel = new JLabel("價格：" + price);
    priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    panel.add(imageItem);
    panel.add(priceLabel);

    // 紀錄food加到menuItem
    this.menuItems.add(new MenuItem(name, price, ""));
    System.out.println("menu items: " + this.menuItems);

    return panel;
  }

  private JPanel createOrderPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(Color.darkGray);

    // 建立選擇區域
    JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    selectionPanel.setBorder(BorderFactory.createTitledBorder("選擇餐點"));

    // 建立餐點選擇器
    itemSelector = new JComboBox<>(menuItems.toArray(new MenuItem[0]));
    // 建立數量選擇器
    quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    // 建立下單按鈕
    addButton = new JButton("下單");

    // 將元件加入選擇面板中
    selectionPanel.add(itemSelector);
    selectionPanel.add(quantitySpinner);
    selectionPanel.add(addButton);

    // 建立購物車面板
    JPanel cartPanel = new JPanel();
    cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.X_AXIS));

    cartPanel.add(selectionPanel);

    // 建立購物車清單面板(可捲動)
    this.cartListPanel = new JPanel();
    this.cartListPanel.setLayout(new BoxLayout(this.cartListPanel, BoxLayout.Y_AXIS));

    // 建立捲動面板
    JScrollPane scrollPane = new JScrollPane(this.cartListPanel);
    scrollPane.setBorder(BorderFactory.createTitledBorder("目前訂單已選項目"));
    scrollPane.setPreferredSize(new Dimension(400, 200));
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    this.cartListPanel.setBorder(null);

    cartPanel.add(scrollPane);
    panel.add(cartPanel);

    // 建立按鈕面板
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBackground(Color.darkGray);

    // 建立送出和處理的按鈕
    orderButton = new JButton("送出訂單");
    processButton = new JButton("處理訂單");

    buttonPanel.add(orderButton);
    buttonPanel.add(processButton);

    panel.add(buttonPanel);

    return panel;
  }

  private JPanel createStatusPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("訂單狀態"));

    // 建立狀態顯示面板
    this.statusDisplay = new JPanel(new GridLayout(1, 3, 10, 0));

    // 等待狀態面板
    JPanel waitingStatusPanel = new JPanel(new GridLayout(2, 1));
    this.waitingStatusLabel = new JLabel("訂單已接收: 0");
    this.waitingTimeLabel = new JLabel("更新時間: " + getCurrentTime());
    waitingStatusPanel.add(this.waitingStatusLabel);
    waitingStatusPanel.add(waitingTimeLabel);
    waitingStatusPanel.setBorder(BorderFactory.createEtchedBorder());

    // 處理狀態面板
    JPanel processingStatusPanel = new JPanel(new GridLayout(2, 1));
    this.processingStatusLabel = new JLabel("正在準備中: 0");
    this.processingTimeLabel = new JLabel("更新時間: " + getCurrentTime());
    processingStatusPanel.add(this.processingStatusLabel);
    processingStatusPanel.add(processingTimeLabel);
    processingStatusPanel.setBorder(BorderFactory.createEtchedBorder());

    // 完成狀態面板
    JPanel completedStatusPanel = new JPanel(new GridLayout(2, 1));
    this.completedStatusLabel = new JLabel("餐點已完成: 0");
    this.completedTimeLabel = new JLabel("更新時間: " + getCurrentTime());
    completedStatusPanel.add(this.completedStatusLabel);
    completedStatusPanel.add(this.completedTimeLabel);
    completedStatusPanel.setBorder(BorderFactory.createEtchedBorder());

    // 加入所有狀態面板
    this.statusDisplay.add(waitingStatusPanel);
    this.statusDisplay.add(processingStatusPanel);
    this.statusDisplay.add(completedStatusPanel);

    panel.add(this.statusDisplay, BorderLayout.CENTER);

    return panel;
  }

  private String getCurrentTime() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private void addListeners() {
    // 新增餐攢按鈕監聽器
    this.addButton.addActionListener(e -> {
      MenuItem item = (MenuItem) itemSelector.getSelectedItem();
      int quantity = (Integer) quantitySpinner.getValue();

      // 建立新的購物車項目
      JPanel newPanel = createCartItem(item, quantity);

      if(newPanel != null) {
        this.cartListPanel.add(newPanel);
      }

      forceUpdateUI();
    });

    // 送出訂單按鈕監聽器
    orderButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // 建立新訂單
        Order order = null;
        for(Map.Entry<MenuItem, Integer> item : cartListItems.entrySet()) {
          if(order == null) {
            order = OrderFactory.createNextOrder(item.getKey(), item.getValue());
          } else {
            order.addItem(item.getKey(), item.getValue());
          }
        }

        if(order != null) {
          producer.addOrder(order);
          System.out.println(order.getId());
        }

        orderButton.setEnabled(false);

        try {
          cartListItems = new HashMap<>();
          cartListPanel.removeAll();
          forceUpdateUI();
          Thread.sleep(500);
        } catch(InterruptedException event) {
          event.printStackTrace();
        }

        orderButton.setEnabled(true);
      }
    });

    // 在新執行緒中監控訂單狀態
    Thread statusMonitor = new Thread(() -> {
      try {
        while(!Thread.currentThread().isInterrupted()) {
          SwingUtilities.invokeLater(() -> {
            updateStatusPanel();
          });
          Thread.sleep(500);
        }
      } catch(InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });
    statusMonitor.setDaemon(true);
    statusMonitor.start();

    // 處理訂單按鈕監聽器
    processButton.addActionListener(e -> {
      // 檢查是否有訂單需要處理
      // if(producer.getQueue().isEmpty()) {
      //   JOptionPane.showMessageDialog(this, "目前沒有待處理的訂單");
      //   return;
      // }
      boolean hasWaitingOrders = producer.getTotalOrders().stream().anyMatch(order -> order.getStatus() == OrderStatus.WAITING);

      if(!hasWaitingOrders) {
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(this, "目前沒有待處理的訂單");
        });
        return;
      }

      // 檢查是否有訂單正在處理
      if(consumer.getProcessingOrder() != null) {
        JOptionPane.showMessageDialog(this, "已有訂單正在處理中，請稍後再試");
        return;
      }

      // 在新執行緒中處理訂單
      new Thread(() -> {
        try {
          // 開始處理下一個訂單
          consumer.processNextOrder();
          consumer.startProcessing();

          // 模擬處理時間
          for(int i = 0; i < 3; i++) {
            Thread.sleep(1000);
            SwingUtilities.invokeLater(this::updateStatusPanel);
          }

          // 完成訂單
          AbstractOrder processingOrder = consumer.getProcessingOrder();
          if(processingOrder != null) {
            consumer.completedOrder(processingOrder);

            // 更新UI並顯示完成訊息
            SwingUtilities.invokeLater(() -> {
              updateStatusPanel();
              JOptionPane.showMessageDialog(this, "訂單 " + processingOrder.getId() + " 處理完成！", "訂單完成", JOptionPane.INFORMATION_MESSAGE);
            });
          }
        } catch(InterruptedException ex) {
          Thread.currentThread().interrupt();
          SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "處理訂單時發生錯誤");
          });
        }
      }).start();
    });
  }

  private void updateStatusPanel() {
    // 更新訂單狀態的數目
    String waitingText = "訂單已接收: " + producer.getTotalOrders().size();
    String processingText = "正在準備中: " + (consumer.getProcessingOrder() == null ? 0 : 1);
    String completedText = "餐點已完成: " + consumer.getCountOfCompletedOrders();

    // 更新 labels
    this.waitingStatusLabel.setText(waitingText);
    this.processingStatusLabel.setText(processingText);
    this.completedStatusLabel.setText(completedText);

    // 更新時間
    String currentTime = getCurrentTime();
    this.waitingTimeLabel.setText("更新時間: " + currentTime);
    this.processingTimeLabel.setText("更新時間: " + currentTime);
    this.completedTimeLabel.setText("更新時間: " + currentTime);

    // 更新訂單金額和今日營業額
    double currentCartTotal = cartListItems.entrySet().stream().mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue()).sum();
    this.orderSummaryPanel.updateTotalAmount(currentCartTotal);
    this.orderSummaryPanel.updateDailyRevenue(consumer.getRevenue());

    // 刷新面板畫面
    statusPanel.revalidate();
    statusPanel.repaint();
  }

  private JPanel createCartItem(MenuItem item, int quantity) {
    // 檢查餐點項目是否已存在在購物車中
    if(this.cartListItems.containsKey(item)) {
      // 更新已存在餐點的數量
      int newQuantity = this.cartListItems.get(item) + quantity;
      this.cartListItems.put(item, newQuantity);

      // 更新購物車面板
      for(Component comp : this.cartListPanel.getComponents()) {
        if(comp instanceof JPanel) {
          JPanel panel = (JPanel) comp;

          for(Component panelComp : panel.getComponents()) {
            if(panelComp instanceof JLabel) {
              JLabel label = (JLabel) panelComp;

              if(label.getText().equals(item.getName())) {
                // 更新數量label
                for(Component quantityComp : panel.getComponents()) {
                  if(quantityComp instanceof JLabel) {
                    JLabel quantityLabel = (JLabel) quantityComp;

                    if(quantityLabel.getText().startsWith("x ")) {
                      quantityLabel.setText("x " + newQuantity);
                      break;
                    }
                  }
                }

                return null;
              }
            }
          }
        }
      }
    } else {
      this.cartListItems.put(item, quantity);
    }

    JPanel cartItem = new JPanel(new FlowLayout(FlowLayout.LEFT));
    cartItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
    cartItem.setPreferredSize(new Dimension(380, 50));

    JLabel nameLabel = new JLabel(item.getName());
    JLabel quantityLabel = new JLabel("x " + this.cartListItems.get(item));

    RoundLabel imgLabel = new RoundLabel("IMG");
    imgLabel.setPreferredSize(new Dimension(40, 40));
    imgLabel.setOpaque(false);
    imgLabel.setBackground(Color.LIGHT_GRAY);

    JButton deleteButton = new JButton("移除");
    deleteButton.setFocusPainted(false);
    deleteButton.setContentAreaFilled(false);
    deleteButton.setPreferredSize(new Dimension(50, 20));

    deleteButton.addActionListener(e -> {
      cartListItems.remove(item);
      cartListPanel.remove(cartItem);
      forceUpdateUI();
    });

    cartItem.add(imgLabel);
    cartItem.add(nameLabel);
    cartItem.add(quantityLabel);
    cartItem.add(deleteButton);

    return cartItem;
  }

  // 載入已儲存的訂單
  private void loadSavedOrders() {
    try {
      List<Order> savedOrders = orderFileManager.readAllOrders();

      if(!savedOrders.isEmpty()) {
        for(Order order : savedOrders) {
          switch (order.getStatus()) {
            case WAITING:
              producer.addOrder(order);
              break;
            case PROCESSING:
              consumer.setProcessingOrder(order);
              new Thread(() -> {
                try {
                  consumer.startProcessing();
                  Thread.sleep(3000);
                  consumer.completedOrder(order);
                  SwingUtilities.invokeLater(() -> {
                    updateStatusPanel();
                    JOptionPane.showMessageDialog(this, "訂單 " + order.getId() + " 處理完成！", "訂單完成", JOptionPane.INFORMATION_MESSAGE);
                  });
                } catch(InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              }).start();
              break;
            case COMPLETED:
              consumer.addCompletedOrder(order);
              break;
            default:
              break;
          }
        }

        updateStatusPanel();
      }
    } catch(Exception e) {
      System.err.println("Error loading saved orders: " + e.getMessage());
    }
  }

  // 強制更新 UI 的方法
  private void forceUpdateUI() {
    // 更新購物車面板
    this.cartListPanel.revalidate();
    this.cartListPanel.repaint();

    // 更新整個視窗
    revalidate();
    repaint();
  }

  private class RoundLabel extends JLabel {
    public RoundLabel(String text) {
      super(text);
      setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // 畫背景
      g2.setColor(getBackground());
      g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

      // 畫文字
      g2.setColor(getForeground());
      super.paintComponent(g2);
      g2.dispose();
    }
  }
}

/*
整合 View 元件（Food, OrderSummaryPanel）
控制點餐邏輯（加入 currentOrder）
按下「Submit」時建立 Order 並呼叫 Controller 負責處理（Producer）
 */