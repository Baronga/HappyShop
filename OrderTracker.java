package ci553.happyshop.client.orderTracker;

import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.orderManagement.OrderState;
import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WinPosManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.TreeMap;

/**
 * OrderTracker class is for tracking orders and their states.
 * It displays an ordersMap(a list of orders with their associated states) in a TextArea.
 * The ordersMap data is received from the OrderHub.
 */

public class OrderTracker implements PropertyChangeListener {

    
    // TreeMap (orderID,state) holding order IDs and their corresponding states.
    private final TreeMap<Integer, OrderState> ordersMap = new TreeMap<>();
    private final TextArea taDisplay = new TextArea(); //area to show all orderId and their state on the GUI

     //Constructor initializes the UI, a title Label, and a TextArea for displaying the order details.
    public OrderTracker() {
        Label title = new Label("Order_ID   State");
        title.setStyle(UIStyle.labelTitleStyle);

    
        taDisplay.setEditable(false);
        taDisplay.setStyle(UIStyle.textFiledStyle);

        VBox root = new VBox(10, title, taDisplay);
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle(UIStyle.rootStyleGray);

        Button btnReset = new Button("Reset Orders");
        btnReset.setOnAction(e -> resetOrders());
        root.getChildren().add(btnReset); // add button to the displayed VBox

        Stage window = new Stage();
        window.setScene(new Scene(root,
                UIStyle.trackerWinWidth,
                UIStyle.trackerWinHeight));

        WinPosManager.registerWindow(window,
                UIStyle.trackerWinWidth,
                UIStyle.trackerWinHeight);

        window.setTitle("🛒 Order Tracker");
        registerWithOrderHub();
        window.show();
        
    }
    
    
    private void resetOrders() {
    OrderHub hub = OrderHub.getOrderHub();
    hub.resetAllOrders(); // this will clear orders from memory and notify UI
}
    /**
     * Registers this OrderTracker instance with the OrderHub.
     * This allows the OrderTracker to receive updates on order state changes.
     */
    // register with order hub
    public void registerWithOrderHub() {
        OrderHub.getOrderHub().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!"orderMap".equals(evt.getPropertyName())) return;

        TreeMap<Integer, OrderState> newMap = (TreeMap<Integer, OrderState>) evt.getNewValue();
        ordersMap.clear();
        ordersMap.putAll(newMap);

        // Make sure UI update happens on JavaFX thread
        Platform.runLater(this::displayOrderMap);
    }
    /**
     * Sets the order map with new data and refreshes the display.
     * This method is called by OrderHub when order states are updated.
     */

     //Displays the current order map in the TextArea.
     //Iterates over the ordersMap and formats each order ID and state for display.
    private void displayOrderMap() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, OrderState> entry : ordersMap.entrySet()) {
            sb.append(entry.getKey()).append("     ").append(entry.getValue()).append("\n");
        }
        taDisplay.setText(sb.toString());
    }
}

