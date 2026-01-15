package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ci553.happyshop.utility.ProductListFormatter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 * You can either directly modify the CustomerModel class to implement the required tasks,
 * or create a subclass of CustomerModel and override specific methods where appropriate.
 */
public class CustomerModel {
    public CustomerView cusView;
    public CustomerView customerView;
    public Product getTheProduct() {
    return theProduct;
}
    public void setSearchResults(ArrayList<Product> products) {
    cusView.lvProducts.getItems().setAll(products);
    }
    public DatabaseRW databaseRW; //Interface type, not specific implementation
                                  //Benefits: Flexibility: Easily change the database implementation.

    private Product theProduct =null; // product found from search
    private ArrayList<Product> trolley =  new ArrayList<>(); // a list of products in trolley
    private ObservableList<Product> observableTrolley = FXCollections.observableArrayList();

    // Four UI elements to be passed to CustomerView for display updates.
    private String imageName = "imageHolder.jpg";                // Image to show in product preview (Search Page)
    private String displayLaSearchResult = "No Product was searched yet"; // Label showing search result message (Search Page)
    private String displayTaTrolley = "";                                // Text area content showing current trolley items (Trolley Page)
    private String displayTaReceipt = "";                                // Text area content showing receipt after checkout (Receipt Page)

    //SELECT productID, description, image, unitPrice,inStock quantity
void search() throws SQLException {

    String productId = cusView.tfId.getText().trim();

    // 1️⃣ No input
    if (productId.isEmpty()) {
        theProduct = null;
        displayLaSearchResult = "Please type ProductID";
        updateView();
        return;
    }

    // 2️⃣ Search database
    theProduct = databaseRW.searchByProductId(productId);

    // 3️⃣ Not found
    if (theProduct == null) {
        displayLaSearchResult = "No Product was found with ID " + productId;
        updateView();
        return;
    }

    // 4️⃣ Found but no stock
    if (theProduct.getStockQuantity() <= 0) {
        displayLaSearchResult = "Product is out of stock";
        theProduct = null;   // 🔴 IMPORTANT: prevents AddToTrolley
        updateView();
        return;
    }

    // 5️⃣ Valid product
    displayLaSearchResult = String.format(
            "Product ID: %s\n%s\nPrice: £%.2f%s",
            theProduct.getProductId(),
            theProduct.getProductDescription(),
            theProduct.getUnitPrice(),
            theProduct.getStockQuantity() < 100
                    ? "\n" + theProduct.getStockQuantity() + " units left."
                    : ""
    );

    updateView();
}



void addToTrolley(Product theProduct) {

    if (theProduct == null) {
        displayLaSearchResult =
                "Please search for an available product before adding it to the trolley";
        updateView();
        return;
    }

    boolean found = false;

    // 1. Merge if already in trolley
    for (Product p : trolley) {
        if (p.getProductId().equals(theProduct.getProductId())) {
            p.setOrderedQuantity(p.getOrderedQuantity() + 1); // 
            found = true;
            break;
        }
    }

    // 2. First time add → create copy with quantity 1
    if (!found) {
        Product copy = new Product(
                theProduct.getProductId(),
                theProduct.getProductDescription(),
                theProduct.getProductImageName(),
                theProduct.getUnitPrice(),
                theProduct.getStockQuantity()
        );
        copy.setOrderedQuantity(1); // 
        trolley.add(copy);
    }

    // 3. Sort by product ID
    trolley.sort((p1, p2) -> p1.getProductId().compareTo(p2.getProductId()));

    // 4. Update display strings
    displayTaTrolley = ProductListFormatter.buildString(trolley);
    displayTaReceipt = ""; // clear receipt

    // 5. Refresh UI
    updateView();
}


    void checkOut() throws IOException, SQLException {
        if(!trolley.isEmpty()){
            // Group the products in the trolley by productId to optimize stock checking
            // Check the database for sufficient stock for all products in the trolley.
            // If any products are insufficient, the update will be rolled back.
            // If all products are sufficient, the database will be updated, and insufficientProducts will be empty.
            // Note: If the trolley is already organized (merged and sorted), grouping is unnecessary.
        ArrayList<Product> groupedTrolley = groupProductsById(trolley);
        ArrayList<Product> insufficientProducts =databaseRW.purchaseStocks(groupedTrolley);

            if(insufficientProducts.isEmpty()){ // If stock is sufficient for all products
                //get OrderHub and tell it to make a new Order
                OrderHub orderHub =OrderHub.getOrderHub();
                Order theOrder = orderHub.newOrder(trolley);
                trolley.clear();
                displayTaTrolley ="";
                displayTaReceipt = String.format(
                        "Order_ID: %s\nOrdered_Date_Time: %s\n%s",
                        theOrder.getOrderId(),
                        theOrder.getOrderedDateTime(),
                        ProductListFormatter.buildString(theOrder.getProductList())
                );
                System.out.println(displayTaReceipt);
            }
            else{ // Some products have insufficient stock — build an error message to inform the customer
                StringBuilder errorMsg = new StringBuilder();
                for(Product p : insufficientProducts){
                    errorMsg.append("\u2022 "+ p.getProductId()).append(", ")
                            .append(p.getProductDescription()).append(" (Only ")
                            .append(p.getStockQuantity()).append(" available, ")
                            .append(p.getOrderedQuantity()).append(" requested)\n");
                }
                theProduct=null;

                //TODO
                // Add the following logic here:
                // 1. Remove products with insufficient stock from the trolley.
                // 2. Trigger a message window to notify the customer about the insufficient stock, rather than directly changing displayLaSearchResult.
                //You can use the provided RemoveProductNotifier class and its showRemovalMsg method for this purpose.
                //remember close the message window where appropriate (using method closeNotifierWindow() of RemoveProductNotifier class)
                displayLaSearchResult = "Checkout failed due to insufficient stock for the following products:\n" + errorMsg.toString();
                System.out.println("stock is not enough");
            }
        }
        else{
            displayTaTrolley = "Your trolley is empty";
            System.out.println("Your trolley is empty");
        }
        updateView();
    }

    /**
     * Groups products by their productId to optimize database queries and updates.
     * By grouping products, we can check the stock for a given `productId` once, rather than repeatedly
     */
    private ArrayList<Product> groupProductsById(ArrayList<Product> proList) {

    Map<String, Product> grouped = new HashMap<>();

    for (Product p : proList) {
        String id = p.getProductId();

        if (grouped.containsKey(id)) {
            Product existing = grouped.get(id);
            existing.setOrderedQuantity(
                existing.getOrderedQuantity() + p.getOrderedQuantity()
            );
        } else {
            Product copy = new Product(
                p.getProductId(),
                p.getProductDescription(),
                p.getProductImageName(),
                p.getUnitPrice(),
                p.getStockQuantity()
            );
            copy.setOrderedQuantity(p.getOrderedQuantity());
            grouped.put(id, copy);
        }
    }
    return new ArrayList<>(grouped.values());
}

    void cancel(){
        trolley.clear();
        displayTaTrolley="";
        updateView();
    }
    void closeReceipt(){
        displayTaReceipt="";
    }

    void updateView() {
        if(theProduct != null){
            imageName = theProduct.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder +imageName; //relative file path, eg images/0001.jpg
            // Get the full absolute path to the image
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            imageName = imageFullPath.toUri().toString(); //get the image full Uri then convert to String
            System.out.println("Image absolute path: " + imageFullPath); // Debugging to ensure path is correct
        }
        else{
            imageName = "imageHolder.jpg";
        }
        cusView.update(imageName, displayLaSearchResult, displayTaTrolley,displayTaReceipt);
    }
     // extra notes:
     //Path.toUri(): Converts a Path object (a file or a directory path) to a URI object.
     //File.toURI(): Converts a File object (a file on the filesystem) to a URI object

public void refreshTrolley() {
    if (cusView != null) {
        cusView.refreshTrolleyList(trolley);
    }
}

    //for test only
    public ArrayList<Product> getTrolley() {
        return trolley;
    }
}
