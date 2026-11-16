package aka.braze.android;

import android.content.Context;
import android.content.SharedPreferences;

import com.braze.models.recommended.ecommerce.EcommerceProduct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Cart {
    private static Cart instance;
    private final Map<String, Product> cartItems;
    private  String cartId;
    private final SharedPreferences prefs;
    private static final String PREF_NAME = "BrazeCartPrefs";
    private static final String KEY_CART_ID = "fixed_cart_id";
    private static final String KEY_CART_ITEMS = "stored_cart_items";
    private Cart(Context context) {
        this.cartItems = new HashMap<>();
        Context appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String savedCartId = prefs.getString(KEY_CART_ID, null);
        if (savedCartId == null) {
            savedCartId = "CART_FIXED_" + UUID.randomUUID().toString();
            prefs.edit().putString(KEY_CART_ID, savedCartId).apply();
        }
        this.cartId = savedCartId;

        loadCartItemsFromStorage();
    }

    public static synchronized Cart getInstance(Context context) {
        if (instance == null) {
            instance = new Cart(context);
        }
        return instance;
    }

    public String getCartId() {
        return cartId;
    }

    public synchronized void addItem(Product product) {
        if (product == null) return;

        String productId = product.getProductId();
        if (cartItems.containsKey(productId)) {
            Product existingProduct = cartItems.get(productId);
            if (existingProduct != null) {
                // Tăng số lượng lên 1 đơn vị (hoặc cộng theo số lượng product truyền vào)
                long newQuantity = existingProduct.getQuantity() + product.getQuantity();
                existingProduct.setQuantity(newQuantity);
            }
        } else {
            Product newProduct = new Product(
                    product.getProductId(),
                    product.getProductName(),
                    product.getVariantId(),
                    product.getPrice(),
                    product.getQuantity() > 0 ? product.getQuantity() : 1L,
                    product.getImageUrl(),
                    product.getProductUrl(),
                    product.getMetadata()
            );
            cartItems.put(productId, newProduct);
        }
        saveCartItemsToStorage();
    }

    public synchronized void removeProduct(Product product) {
        if (product == null) return;

        String productId = product.getProductId();
        if (cartItems.containsKey(productId)) {
            cartItems.remove(productId);

            saveCartItemsToStorage();
        }
    }

    public java.util.Collection<Product> getCartProducts() {
        return cartItems.values();
    }

    public List<EcommerceProduct> toBrazeProductsList() {
        List<EcommerceProduct> brazeProducts = new ArrayList<>();

        for (Product p : cartItems.values()) {
            EcommerceProduct bp = new EcommerceProduct(
                    p.getProductId(),
                    p.getProductName(),
                    p.getVariantId(),
                    p.getPrice(),
                    (int) p.getQuantity(),
                    p.getImageUrl(),
                    p.getProductUrl(),
                    p.getMetadata()
            );
            brazeProducts.add(bp);
        }
        return brazeProducts;
    }

    public void clear() {
        cartItems.clear();
        saveCartItemsToStorage();

        String newCartId = "CART_FIXED_" + UUID.randomUUID().toString().substring(0, 8);
        prefs.edit().putString(KEY_CART_ID, newCartId).apply();
        this.cartId = newCartId;

        android.util.Log.d("[BrazeCart]", "🔄 Giỏ hàng local đã được dọn sạch. Đã cấp phát Cart ID mới: " + this.cartId);
    }

    private void saveCartItemsToStorage() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Product p : cartItems.values()) {
                JSONObject obj = new JSONObject();
                obj.put("id", p.getProductId());
                obj.put("name", p.getProductName());
                obj.put("variant", p.getVariantId());
                obj.put("quantity", p.getQuantity());
                obj.put("price", p.getPrice());
                obj.put("img", p.getImageUrl());
                obj.put("url", p.getProductUrl());
                jsonArray.put(obj);
            }
            prefs.edit().putString(KEY_CART_ITEMS, jsonArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCartItemsFromStorage() {
        String json = prefs.getString(KEY_CART_ITEMS, null);
        if (json == null) return;
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Product p = new Product(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.getString("variant"),
                        obj.getDouble("price"),
                        obj.getLong("quantity"),
                        obj.optString("img", null),
                        obj.optString("url", null),
                        null
                );
                cartItems.put(p.getProductId(), p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}