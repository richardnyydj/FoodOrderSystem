package com.ordersystem.model;

public enum OrderStatus {
  WAITING, PROCESSING, COMPLETED, CANCELLED
}

/*
用途：定義訂單的三種狀態。
使用場景：訂單從建立（NEW）→ 處理中（IN_PROGRESS）→ 完成（COMPLETED）。
好處：用 enum 取代字串比對，更安全、易於維護。
 */