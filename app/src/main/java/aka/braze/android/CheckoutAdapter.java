package aka.braze.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private final List<Product> products;

    public CheckoutAdapter(List<Product> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        Product p = products.get(position);
        holder.txtId.setText(p.getProductId());
        holder.txtQty.setText("Số lượng: " + p.getQuantity());
    }

    @Override
    public int getItemCount() { return products.size(); }

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtQty;
        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtCheckoutProductId);
            txtQty = itemView.findViewById(R.id.txtCheckoutProductQuantity);
        }
    }
}