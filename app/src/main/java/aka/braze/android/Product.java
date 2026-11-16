package aka.braze.android;

import com.braze.models.outgoing.BrazeProperties;
import java.io.Serializable;

public class Product implements Serializable {
    private String productId;
    private String productName;
    private String variantId;
    private double price;
    private long quantity;
    private String imageUrl;
    private String productUrl;
    private BrazeProperties metadata;

    public Product(String productId, String productName, String variantId, double price, long quantity,
                   String imageUrl, String productUrl, BrazeProperties metadata) {
        this.productId = productId;
        this.productName = productName;
        this.variantId = variantId;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
        this.metadata = metadata;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getVariantId() { return variantId; }
    public double getPrice() { return price; }
    public long getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
    public String getProductUrl() { return productUrl; }
    public BrazeProperties getMetadata() { return metadata; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
}