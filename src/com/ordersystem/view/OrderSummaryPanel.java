package com.ordersystem.view;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class OrderSummaryPanel extends JPanel {
  private final JLabel totalAmountLabel;
  private final JLabel dailyRevenueLabel;

  public OrderSummaryPanel() {
    this.totalAmountLabel = new JLabel("總金額: $0.00");
    this.dailyRevenueLabel = new JLabel("今日營業額: $0.00");

    MatteBorder matteBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, Color.GRAY);
    TitledBorder title = BorderFactory.createTitledBorder(matteBorder, "訂單摘要");

    title.setTitleJustification(TitledBorder.CENTER);
    setBorder(title);

    add(this.totalAmountLabel);
    add(this.dailyRevenueLabel);
  }

  public void updateTotalAmount(double amount) {
    totalAmountLabel.setText(String.format("總金額: $%.2f", amount));
  }

  public void updateDailyRevenue(double revenue) {
    dailyRevenueLabel.setText(String.format("今日營業額: $%.2f", revenue));
  }
}

/*
接收目前已點選的 List<MenuItem> 更新 UI
顯示品項文字清單與總價
是典型的觀察者更新介面，但目前為手動呼叫 updateSummary()
 */