package aka.braze.android;

import static com.braze.models.recommended.ecommerce.CartUpdatedAction.REPLACE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.braze.models.recommended.ecommerce.CartUpdatedAction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckoutFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView txtTotalValue;
    private Button btnOrderPlaced;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCheckout);
        txtTotalValue = view.findViewById(R.id.txtCheckoutTotalValue);
        btnOrderPlaced = view.findViewById(R.id.btnOrderPlaced);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupCheckoutData();

        btnOrderPlaced.setOnClickListener(v -> {
            // Logic khi user hoàn tất thanh toán
            handlePurchase();
        });

        return view;
    }

    private void setupCheckoutData() {
        List<Product> list = new ArrayList<>(Cart.getInstance(requireContext()).getCartProducts());

        double total = 0;
        for (Product p : list) {
            total += (p.getPrice() * p.getQuantity());
        }
        txtTotalValue.setText(String.format("%,.0f USD", total));

        CheckoutAdapter adapter = new CheckoutAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    private void handlePurchase() {
        try {
            android.content.Context context = requireContext();
            List<Product> localProducts = new java.util.ArrayList<>(Cart.getInstance(context).getCartProducts());

            if (localProducts == null || localProducts.isEmpty()) {
                Toast.makeText(context, "Không thể đặt hàng vì giỏ hàng trống trơn!", Toast.LENGTH_SHORT).show();
                android.util.Log.e("[BrazeBug]", "❌ [CHẶN] Ngừng xử lý OrderPlacedEvent vì mảng sản phẩm rỗng.");
                return;
            }

            java.util.ArrayList<com.braze.models.recommended.ecommerce.EcommerceProduct> brazeProductsList = new java.util.ArrayList<>();
            double totalValue = 0.0;

            com.braze.models.outgoing.BrazeProperties standardMetadata = new com.braze.models.outgoing.BrazeProperties();

            for (Product p : localProducts) {
                totalValue += (p.getPrice() * p.getQuantity());

                String productId = (p.getProductId() != null && !p.getProductId().isEmpty()) ? p.getProductId() : "UNKNOWN_ID";
                String productName = (p.getProductName() != null && !p.getProductName().isEmpty()) ? p.getProductName() : "Unknown Product";
                String variantId = (p.getVariantId() != null && !p.getVariantId().isEmpty()) ? p.getVariantId() : "default_variant";
                String imageUrl = (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) ? p.getImageUrl() : "https://braze.com/default.png";
                String productUrl = (p.getProductUrl() != null && !p.getProductUrl().isEmpty()) ? p.getProductUrl() : "https://braze.com";

                brazeProductsList.add(new com.braze.models.recommended.ecommerce.EcommerceProduct(
                        productId,
                        productName,
                        variantId,
                        p.getPrice(),
                        (int) p.getQuantity(),
                        imageUrl,
                        productUrl,
                        standardMetadata
                ));
            }

            String cartId = Cart.getInstance(context).getCartId();
            String orderId = "ORDER_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            debugBrazeProductsBeforeLogging(brazeProductsList, cartId);

            java.util.List<Object> nonNullDiscountsList = new java.util.ArrayList<>();

            try {
                java.lang.reflect.Constructor<?> constructor9 = com.braze.models.recommended.ecommerce.OrderPlacedEvent.class
                        .getDeclaredConstructor(
                                String.class,
                                String.class,
                                String.class,
                                double.class,
                                java.util.List.class,
                                String.class,
                                Double.class,
                                java.util.List.class,
                                com.braze.models.outgoing.BrazeProperties.class
                        );

                constructor9.setAccessible(true);

                com.braze.models.recommended.ecommerce.OrderPlacedEvent orderPlacedEvent =
                        (com.braze.models.recommended.ecommerce.OrderPlacedEvent) constructor9.newInstance(
                                orderId,
                                "USD",
                                "Checkout_Screen",
                                totalValue,
                                brazeProductsList,
                                cartId,
                                Double.valueOf(0.0),
                                nonNullDiscountsList,
                                standardMetadata
                        );
                com.braze.Braze.getInstance(context).logEcommerceEvent(orderPlacedEvent);
                android.util.Log.d("[BrazeCheckoutFragment]", "🚀 Đã gọi OrderPlacedEvent");

            } catch (Exception eReflection) {
                android.util.Log.e("[BrazeBug]", "❌ Thất bại khi bẫy Reflection: " + eReflection.getMessage());
                eReflection.printStackTrace();
            }

            try {
                com.braze.models.recommended.ecommerce.CartUpdatedEvent clearCartEvent =
                        new com.braze.models.recommended.ecommerce.CartUpdatedEvent(
                                cartId, "USD", "Checkout_Success_Clear", 0.0,
                                new java.util.ArrayList<>(), null, REPLACE
                        );
                com.braze.Braze.getInstance(context).logEcommerceEvent(clearCartEvent);
                android.util.Log.d("[BrazeCheckoutFragment]", "🚀 Đã gọi clearCartEvent");
            } catch (Exception eClearCart) {
//                android.util.Log.w("[BrazeBug]", "⚠️ Chấp nhận bỏ qua lỗi Validate của ClearCartEvent để giữ thông luồng: " + eClearCart.getMessage());
            }

            com.braze.Braze.getInstance(context).requestImmediateDataFlush();

            Cart.getInstance(context).clear();
            Toast.makeText(context, "Đặt hàng thành công! Đơn hàng: " + orderId, Toast.LENGTH_LONG).show();

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProductFragment())
                        .commit();
            }

        } catch (Exception e) {
            android.util.Log.e("[BrazeCheckoutFragment]", "❌ Lỗi hệ thống phát sinh ngoài luồng mong muốn: " + e.getMessage());
        }
    }

    private void debugBrazeProductsBeforeLogging(java.util.List<com.braze.models.recommended.ecommerce.EcommerceProduct> products, String cartId) {
        android.util.Log.d("[BrazeInspector]", "==================================================");
        android.util.Log.d("[BrazeInspector]", "🔍 Kiểm tra sản phẩm trước khi gọi OrderPlaced. 🛒 Cart ID: " + (cartId != null ? cartId : "NULL"));

        if (products == null) {
            android.util.Log.e("[BrazeInspector]", "❌ Mảng sản phẩm truyền vào bị NULL!");
            android.util.Log.d("[BrazeInspector]", "==================================================");
            return;
        }

        android.util.Log.d("[BrazeInspector]", "• Số lượng phần tử thực tế trong mảng: " + products.size());

        for (int i = 0; i < products.size(); i++) {
            com.braze.models.recommended.ecommerce.EcommerceProduct p = products.get(i);

            String id = p.getProductId();
            long qty = p.getQuantity();
            double price = p.getPrice();

            String status = "✅ HỢP LỆ";
            if (id == null || id.trim().isEmpty() || id.equals("UNKNOWN_ID") || id.equals("PROD_UNKNOWN")) {
                status = "⚠️ CẢNH BÁO: ID rỗng hoặc dummy";
            } else if (qty <= 0) {
                status = "❌ LỖI NẶNG: Số lượng (Quantity) phải >= 1 (Hiện tại: " + qty + ")";
            } else if (price <= 0) {
                status = "❌ LỖI NẶNG: Giá (Price) phải lớn hơn 0 (Hiện tại: " + price + ")";
            }

            android.util.Log.d("[BrazeInspector]", String.format("   👉 [%d] Product ID: %s | Quantity: %d | Price: %,.2f USD -> Trạng thái: %s",
                    i, id, qty, price, status));
        }
        android.util.Log.d("[BrazeInspector]", "==================================================");
    }
}