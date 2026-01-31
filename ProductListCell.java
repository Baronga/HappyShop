package ci553.happyshop.client.customer;

import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import ci553.happyshop.catalogue.Product;

public class ProductListCell extends ListCell<Product> {
    HBox container = new HBox(5);
    Text productInfo = new Text();
    Button btnIncrease = new Button("+");
    Button btnDecrease = new Button("−");
    Button btnRemove = new Button("Remove");

    public ProductListCell() {
        container.getChildren().addAll(productInfo, btnIncrease, btnDecrease, btnRemove);

        btnIncrease.setOnAction(e -> {
            if (getItem() != null) {
                getItem().setOrderedQuantity(getItem().getOrderedQuantity() + 1);
                updateItem(getItem(), false);
            }
        });

        btnDecrease.setOnAction(e -> {
            if (getItem() != null && getItem().getOrderedQuantity() > 1) {
                getItem().setOrderedQuantity(getItem().getOrderedQuantity() - 1);
                updateItem(getItem(), false);
            }
        });

        btnRemove.setOnAction(e -> {
            if (getItem() != null) {
                getListView().getItems().remove(getItem());
            }
        });
    }

    @Override
    protected void updateItem(Product item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            productInfo.setText(item.getProductDescription() + " x" + item.getOrderedQuantity());
            setGraphic(container);
        }
    }
}