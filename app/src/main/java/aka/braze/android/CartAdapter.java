package aka.braze.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final List<Product> cartProducts;
    public CartAdapter(List<Product> cartProducts) {
        this.cartProducts = cartProducts;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartProducts.get(position);
        holder.txtCartProductId.setText(product.getProductId());
        holder.txtCartProductQuantity.setText("Qty: " + product.getQuantity());

        holder.btnDeleteProduct.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Product clickedProduct = cartProducts.get(currentPosition);
            android.content.Context context = v.getContext();

            Cart.getInstance(context).removeProduct(clickedProduct);
            trackBrazeCartRemoved(context, clickedProduct);

            cartProducts.remove(currentPosition);
            notifyItemRemoved(currentPosition);
            notifyItemRangeChanged(currentPosition, cartProducts.size());

            Toast.makeText(context, "Đã xóa local ID: " + clickedProduct.getProductId(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return cartProducts != null ? cartProducts.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView txtCartProductId, txtCartProductQuantity;
        Button btnDeleteProduct;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCartProductId = itemView.findViewById(R.id.txtCartProductId);
            txtCartProductQuantity = itemView.findViewById(R.id.txtCartProductQuantity);
            btnDeleteProduct = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }

    private void trackBrazeCartRemoved(android.content.Context context, Product clickedProduct) {
        try {
            String currentCartId = Cart.getInstance(context).getCartId();
            android.util.Log.d("[BrazeProductDetail]", "👉 Xóa " + clickedProduct.getProductId() + " khỏi giỏ hàng "+ currentCartId );

            android.util.Log.d("[BrazeProductDetail]","____________________________________________________");
            for (Product p : Cart.getInstance(context).getCartProducts()) {
                android.util.Log.d("[BrazeCartAdapter]", "   • ID: " + p.getProductId() + " | Tên: " + p.getProductName() + " | Số lượng: " + p.getQuantity());
            }
            android.util.Log.d("[BrazeProductDetail]","____________________________________________________");

            com.braze.models.outgoing.BrazeProperties standardMetadata = new com.braze.models.outgoing.BrazeProperties();

            java.util.List<com.braze.models.recommended.ecommerce.EcommerceProduct> removedProduct = new java.util.ArrayList<>();
            removedProduct.add(new com.braze.models.recommended.ecommerce.EcommerceProduct(
                    clickedProduct.getProductId(),
                    clickedProduct.getProductName(),
                    clickedProduct.getVariantId(),
                    clickedProduct.getPrice(),
                    (int) clickedProduct.getQuantity(),
                    clickedProduct.getImageUrl(),
                    clickedProduct.getProductUrl(),
                    standardMetadata
            ));

            com.braze.models.recommended.ecommerce.CartUpdatedEvent removeEvent =
                    new com.braze.models.recommended.ecommerce.CartUpdatedEvent(
                            currentCartId,
                            "USD",
                            "Cart_Fragment_Row_Delete",
                            null,
                            removedProduct,
                            null,
                            com.braze.models.recommended.ecommerce.CartUpdatedAction.REMOVE
                    );

            com.braze.Braze.getInstance(context).logEcommerceEvent(removeEvent);
            android.util.Log.d("[BrazeCartFragment]", "🚀 Đã gọi CartUpdatedEvent.REMOVE");

        } catch (Exception e) {
            android.util.Log.e("[BrazeCartAdapter]", "❌ Lỗi phát sinh trong luồng Remove Cart Tracking: " + e.getMessage());
            e.printStackTrace();
        }
    }
}