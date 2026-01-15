package ci553.happyshop.client.customer;

import java.io.IOException;
import java.sql.SQLException;

import ci553.happyshop.catalogue.Product;

public class CustomerController {
    public CustomerModel cusModel;
    private CustomerView cusView;

    public void doAction(String action) throws SQLException, IOException {
        switch (action) {
            case "Search":
                cusModel.search();
                break;
            case "Add to Trolley":
        cusModel.addToTrolley(cusModel.getTheProduct());
    break;
            case "Cancel":
                cusModel.cancel();
                break;
            case "Check Out":
                cusModel.checkOut();
                break;
            case "OK & Close":
                cusModel.closeReceipt();
                break;
        }
    }
public void changeQuantity(Product product, int delta) {
    if (product == null) return;

    int newQty = product.getOrderedQuantity() + delta;
    if (newQty > 0) {
        product.setOrderedQuantity(newQty);
    } else {
        product.setOrderedQuantity(1);
    }

    // Refresh via model
    cusModel.refreshTrolley();
}

public void removeFromTrolley(Product product) {
    if (product != null) {
        cusModel.getTrolley().remove(product);

        // Refresh via model
        cusModel.refreshTrolley();
    }
}



}
