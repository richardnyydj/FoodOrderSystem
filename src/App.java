import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

import com.ordersystem.controller.Consumer;
import com.ordersystem.controller.Producer;
import com.ordersystem.model.AbstractOrder;
import com.ordersystem.view.OrderSystemUI;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        BlockingQueue<AbstractOrder> queue = new LinkedBlockingQueue<AbstractOrder>();

        Producer producer = new Producer(queue);
        Consumer consumer = new Consumer(queue, producer);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                OrderSystemUI orderSystemUI = new OrderSystemUI(producer, consumer);
                orderSystemUI.setVisible(true);
            }
        });
    }
}
