package aka.braze.android;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;
    private static final String APP_CURRENCY = "USD";

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.txtProductName.setText(product.getProductName());
        holder.txtProductPrice.setText(String.format("%s %,.2f", APP_CURRENCY, product.getPrice()));

        Glide.with(context)
                .load(product.getImageUrl()) // Truyền link URL dạng String vào đây
                .placeholder(android.R.drawable.ic_menu_gallery) // Ảnh hiện tạm trong lúc đang tải từ mạng
                .error(android.R.drawable.stat_notify_error)     // Ảnh hiện lên nếu link bị lỗi hoặc chết link
                .into(holder.imgProduct); // Chỉ định mục tiêu hiển thị lên ImageView nào

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Product clickedProduct = productList.get(currentPosition);

                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("PRODUCT_DATA", clickedProduct);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { return productList.size(); }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName, txtProductPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
        }
    }
}

