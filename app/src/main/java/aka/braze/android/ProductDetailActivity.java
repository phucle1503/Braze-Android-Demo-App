package aka.braze.android;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.braze.Braze;
import com.braze.models.recommended.ecommerce.CartUpdatedAction;
import com.braze.models.recommended.ecommerce.CartUpdatedEvent;
import com.braze.models.recommended.ecommerce.ProductViewedEvent;
import com.braze.models.recommended.ecommerce.EcommerceProduct;
import com.bumptech.glide.Glide;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgDetailProduct;
    private TextView txtDetailName, txtDetailPrice, txtDetailSchema;
    private Button btnAddToCart;
    private Product currentProduct;
    private static final String APP_CURRENCY = "USD";
    private static final String EVENT_SOURCE = "Tab_Product_List";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();

        currentProduct = (Product) getIntent().getSerializableExtra("PRODUCT_DATA");
        if (currentProduct != null) {
            bindProductData();
            trackBrazeProductViewed();
        } else {
            Log.e("ProductDetail", "Dữ liệu Product truyền sang bị NULL!");
        }
    }

    private void initViews() {
        imgDetailProduct = findViewById(R.id.imgDetailProduct);
        txtDetailName = findViewById(R.id.txtDetailName);
        txtDetailPrice = findViewById(R.id.txtDetailPrice);
        txtDetailSchema = findViewById(R.id.txtDetailSchema);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        btnAddToCart.setOnClickListener(v -> {
            trackBrazeCartAdded();
        });
    }

    private void trackBrazeCartAdded() {
        try {
            Cart.getInstance(this).addItem(currentProduct);
            String currentCartId = Cart.getInstance(this).getCartId();
            android.util.Log.d("[BrazeProductDetail]", "👉 Thêm " + currentProduct.getProductId() + " vào giỏ hàng "+ currentCartId );

//            android.util.Log.d("[BrazeProductDetail]", "📦 [LIST-LOCAL] (Cart ID: " + Cart.getInstance(this).getCartId() + "):");
            android.util.Log.d("[BrazeProductDetail]","____________________________________________________");
            for (Product p : Cart.getInstance(this).getCartProducts()) {
                android.util.Log.d("[BrazeProductDetail]", "   • ID: " + p.getProductId() + " | Tên: " + p.getProductName() + " | Số lượng: " + p.getQuantity());
            }
            android.util.Log.d("[BrazeProductDetail]","____________________________________________________");

            java.util.List<com.braze.models.recommended.ecommerce.EcommerceProduct> addedProduct = new java.util.ArrayList<>();
            addedProduct.add(new com.braze.models.recommended.ecommerce.EcommerceProduct(
                    currentProduct.getProductId(),
                    currentProduct.getProductName(),
                    currentProduct.getVariantId(),
                    currentProduct.getPrice(),
                    (int) currentProduct.getQuantity(),
                    currentProduct.getImageUrl(),
                    currentProduct.getProductUrl(),
                    currentProduct.getMetadata()
            ));

            com.braze.models.recommended.ecommerce.CartUpdatedEvent cartUpdatedEvent =
                    new com.braze.models.recommended.ecommerce.CartUpdatedEvent(
                            Cart.getInstance(this).getCartId(),
                            "USD",
                            "Product_Detail_Page",
                            null,
                            addedProduct,
                            null,
                            com.braze.models.recommended.ecommerce.CartUpdatedAction.ADD
                    );

            com.braze.Braze.getInstance(this).logEcommerceEvent(cartUpdatedEvent);
            android.util.Log.d("[BrazeProductDetail]", "🚀 Đã gọi CartUpdatedEvent.ADD ");

            Toast.makeText(this, "Braze Tracker: Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();

        } catch (IllegalArgumentException e) {
            android.util.Log.e("[Braze]", "Dữ liệu đầu vào không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindProductData() {
        txtDetailName.setText(currentProduct.getProductName());
        txtDetailPrice.setText(String.format("%s %,.2f", APP_CURRENCY, currentProduct.getPrice()));

        if (currentProduct.getImageUrl() != null && !currentProduct.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(currentProduct.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery) // Ảnh tạm trong lúc đợi tải
                    .error(android.R.drawable.stat_notify_error)     // Ảnh hiển thị nếu link bị lỗi hoàn toàn
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e,
                                                    Object model,
                                                    com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                    boolean isFirstResource) {
                            android.util.Log.e("GLIDE_DEBUG", "Tải ảnh thất bại cho sản phẩm: " + currentProduct.getProductName());
                            if (e != null) {
                                e.logRootCauses("GLIDE_DEBUG");
                            }
                            return false; // Trả về false để Glide tiếp tục tự động hiển thị ảnh error lên ImageView
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                       Object model,
                                                       com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                       com.bumptech.glide.load.DataSource dataSource,
                                                       boolean isFirstResource) {
                            // Tải ảnh thành công, không cần xử lý gì thêm
                            android.util.Log.d("GLIDE_DEBUG", "Tải ảnh thành công!");
                            return false;
                        }
                    })
                    .into(imgDetailProduct);
        } else {
            imgDetailProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        String schemaDebug = "Braze PRD Schema Mapping:\n" +
                "• productId: " + currentProduct.getProductId() + "\n" +
                "• productName: " + currentProduct.getProductName() + "\n" +
                "• variantId: " + currentProduct.getVariantId() + "\n" +
                "• price: " + currentProduct.getPrice() + "\n" +
                "• currency: " + APP_CURRENCY + " (Cart Level) \n" +
                "• source: " + EVENT_SOURCE + " (Event Level)";
        txtDetailSchema.setText(schemaDebug);
    }
    private void trackBrazeProductViewed() {
        try {
            ProductViewedEvent viewedEvent = new ProductViewedEvent(
                    currentProduct.getProductId(),
                    currentProduct.getProductName(),
                    currentProduct.getVariantId(),
                    currentProduct.getPrice(),
                    APP_CURRENCY,
                    EVENT_SOURCE,
                    currentProduct.getImageUrl(),
                    currentProduct.getProductUrl(),
                    null
            );

            Braze.getInstance(this).logEcommerceEvent(viewedEvent);

            Toast.makeText(this, "Braze Dispatched: ecommerce.product_viewed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}