package aka.braze.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Chia 2 cột lưới đẹp mắt

        // Khởi tạo data giả lập
        List<Product> products = new ArrayList<>();

        products.add(new Product(
                "PROD_001",
                "iPhone 15 Pro",
                "VAR_COLOR_TITAN",
                999.00,
                1L,
                "https://images.unsplash.com/photo-1736173155811-e8142fd553ee?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "https://aka.vn/products/iphone15",
                null
        ));

        products.add(new Product(
                "PROD_002",
                "MacBook Air M3",
                "VAR_RAM_16GB",
                1299.00,
                1L,
                "https://images.unsplash.com/photo-1651241680016-cc9e407e7dc3?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "https://aka.vn/products/macbookm3",
                null
        ));
        products.add(new Product(
                "PROD_003",
                "Apple Watch Ultra 2",
                "VAR_BAND_TRAIL_LOOP",
                799.00,
                1L,
                "https://images.unsplash.com/photo-1656053209629-8bdf3e494f91?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "https://aka.vn/products/watchultra2",
                null
        ));

        products.add(new Product(
                "PROD_004",
                "AirPods Pro 2 (USB-C)",
                "VAR_CASE_ENGRAVED",
                249.00,
                1L,
                "https://images.unsplash.com/photo-1606741965326-cb990ae01bb2?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "https://aka.vn/products/airpodspro2",
                null
        ));

        products.add(new Product(
                "PROD_005",
                "iPad Pro M4 11-inch",
                "VAR_STORAGE_256GB",
                999.00,
                1L,
                "https://images.unsplash.com/photo-1569697150535-94fb89f890be?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "https://aka.vn/products/ipadprom4",
                null
        ));
        adapter = new ProductAdapter(getContext(), products);
        recyclerView.setAdapter(adapter);

        return view;
    }
}