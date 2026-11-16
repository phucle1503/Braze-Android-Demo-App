package aka.braze.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView recyclerViewCart;
    private CartAdapter adapter;
    private Button btnCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        btnCheckout = view.findViewById(R.id.btnCheckout);

        btnCheckout.setOnClickListener(v -> {
            try {
                android.content.Context context = requireContext();
                List<com.braze.models.recommended.ecommerce.EcommerceProduct> brazeProductsList =
                        Cart.getInstance(context).toBrazeProductsList();

                if (brazeProductsList.isEmpty()) {
                    Toast.makeText(context, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double totalValue = 0.0;
                java.util.ArrayList<com.braze.models.recommended.ecommerce.EcommerceProduct> checkedProducts = new java.util.ArrayList<>();
                for (Product p : new java.util.ArrayList<>(Cart.getInstance(context).getCartProducts())) {
                    totalValue += (p.getPrice() * p.getQuantity());
                    checkedProducts.add(new com.braze.models.recommended.ecommerce.EcommerceProduct(
                            p.getProductId() != null ? p.getProductId() : "",
                            p.getProductName() != null ? p.getProductName() : "",
                            p.getVariantId() != null ? p.getVariantId() : "",
                            p.getPrice(), (int) p.getQuantity(),
                            p.getImageUrl() != null ? p.getImageUrl() : "",
                            p.getProductUrl() != null ? p.getProductUrl() : "",
                            null
                    ));
                }

                String cartId = Cart.getInstance(context).getCartId();
                String checkoutId = "CHECKOUT_" + java.util.UUID.randomUUID().toString().substring(0, 8);

                com.braze.models.recommended.ecommerce.CheckoutStartedEvent checkoutStartedEvent =
                        new com.braze.models.recommended.ecommerce.CheckoutStartedEvent(
                                checkoutId,
                                "USD",
                                "Cart_Screen",
                                totalValue,
                                checkedProducts,
                                cartId,
                                null
                        );
                com.braze.Braze.getInstance(context).logEcommerceEvent(checkoutStartedEvent);
                android.util.Log.d("[BrazeCartFragment]", "🚀 Đã gọi CheckoutStartedEvent ");

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new CheckoutFragment())
                            .addToBackStack(null)
                            .commit();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCartData();
    }

    private void loadCartData() {
        List<Product> currentCartList = new ArrayList<>(Cart.getInstance(requireContext()).getCartProducts());

        adapter = new CartAdapter(currentCartList);
        recyclerViewCart.setAdapter(adapter);
    }
}