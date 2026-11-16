package aka.braze.android;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.braze.Braze;
import com.braze.BrazeUser;
import com.braze.events.IValueCallback;

import org.json.JSONArray;
import org.json.JSONObject;

public class IdentityFragment extends Fragment {
    private EditText editCustomUserId, editFirstName, editLastName, editEmail, editPhoneBody, editHomeCity, editCountry;
    private EditText editDobDay, editDobMonth, editDobYear;
    private Spinner spinnerCountryCode, spinnerGender, spinnerLanguage;
    private Button btnChangeIdentity, btnSyncAttributes;
    private static final String TAG = "[BrazeIdentity]";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_identity, container, false);

        editCustomUserId = view.findViewById(R.id.editCustomUserId);
        editFirstName = view.findViewById(R.id.editFirstName);
        editLastName = view.findViewById(R.id.editLastName);
        editEmail = view.findViewById(R.id.editEmail);
        editPhoneBody = view.findViewById(R.id.editPhoneBody);
        editHomeCity = view.findViewById(R.id.editHomeCity);
        editCountry = view.findViewById(R.id.editCountry);
        editDobDay = view.findViewById(R.id.editDobDay);
        editDobMonth = view.findViewById(R.id.editDobMonth);
        editDobYear = view.findViewById(R.id.editDobYear);

        spinnerCountryCode = view.findViewById(R.id.spinnerCountryCode);
        spinnerGender = view.findViewById(R.id.spinnerGender);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);

        btnChangeIdentity = view.findViewById(R.id.btnChangeIdentity);
        btnSyncAttributes = view.findViewById(R.id.btnSyncAttributes);

        setupAdapters();

        btnChangeIdentity.setOnClickListener(v -> {
            String uid = editCustomUserId.getText().toString().trim();
            if (!uid.isEmpty()) {
                Braze.getInstance(requireContext()).changeUser(uid);
                MyApplication.updateUserId(uid);
                Log.d(TAG, "🔄 changeUser: " + uid);
                Toast.makeText(requireContext(), "Đã chuyển đổi định danh sang: " + uid, Toast.LENGTH_SHORT).show();
            } else {
                editCustomUserId.setError("Vui lòng nhập User ID");
            }
        });

        btnSyncAttributes.setOnClickListener(v -> {
            String countryCode = spinnerCountryCode.getSelectedItem().toString();
            String phoneBody = editPhoneBody.getText().toString().trim();
            String fullPhoneNumber = "";

            if (!phoneBody.isEmpty()) {
                if (phoneBody.startsWith("0")) {
                    phoneBody = phoneBody.substring(1);
                }
                phoneBody = phoneBody.replaceAll("[^0-9]", "");

                fullPhoneNumber = countryCode + phoneBody;
            }
//            Log.d(TAG, "📞 [PHONE_SYNC] Nguyên bản body: " + editPhoneBody.getText().toString().trim()
//                    + " | Định dạng chuẩn gửi Braze: " + (fullPhoneNumber.isEmpty() ? "N/A" : fullPhoneNumber));

            String selectedGenderRaw = spinnerGender.getSelectedItem().toString();
            String genderCode = selectedGenderRaw.substring(selectedGenderRaw.indexOf("(") + 1, selectedGenderRaw.indexOf(")"));

            String selectedLangRaw = spinnerLanguage.getSelectedItem().toString();
            String langCode = selectedLangRaw.substring(selectedLangRaw.indexOf("(") + 1, selectedLangRaw.indexOf(")"));

            BrazeUserService.updateUserProfile(
                    requireContext(),
                    editFirstName.getText().toString().trim(),
                    editLastName.getText().toString().trim(),
                    editEmail.getText().toString().trim(),
                    fullPhoneNumber,
                    genderCode,
                    editHomeCity.getText().toString().trim(),
                    editCountry.getText().toString().trim(),
                    langCode
            );

            String dayStr = editDobDay.getText().toString().trim();
            String monthStr = editDobMonth.getText().toString().trim();
            String yearStr = editDobYear.getText().toString().trim();

            if (!dayStr.isEmpty() && !monthStr.isEmpty() && !yearStr.isEmpty()) {
                try {
                    int day = Integer.parseInt(dayStr);
                    int month = Integer.parseInt(monthStr);
                    int year = Integer.parseInt(yearStr);

                    BrazeUserService.setUserDateOfBirth(requireContext(), year, month, day);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "⚠️ Định dạng ngày sinh nhập vào không phải là số hợp lệ.");
                }
            }

            testSetCustomAttributeJsonObject();
            testSetCustomAttributeArray();
            testSetCustomAttributeJsonArray();

            Toast.makeText(requireContext(), "Đang tiến hành đồng bộ dữ liệu lên Braze", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void testSetCustomAttributeJsonObject() {
        try {
            BrazeUser brazeUser = Braze.getInstance(requireContext()).getCurrentUser();
            if (brazeUser == null) return;

            // 1. Tạo tầng sâu nhất trước (Tầng thứ 15)
            JSONObject currentObject = new JSONObject();
            currentObject.put("level_50_key", "Chạm đáy nỗi đau");
            currentObject.put("status", "Deepest point reached 50");
            // 2. Chạy vòng lặp từ tầng 14 ngược về tầng 2 để lồng đối tượng vào nhau
            for (int i = 49; i >= 2; i--) {
                JSONObject parentObject = new JSONObject();
                parentObject.put("level_" + i + "_meta", "Data for level " + i);

                // Nạp object con của tầng phía sau vào làm thuộc tính của tầng hiện tại
                parentObject.put("nested_level_" + (i + 1), currentObject);

                // Cập nhật lại đối tượng hiện tại thành đối tượng cha vừa tạo
                currentObject = parentObject;
            }

            JSONObject ecommercePrefs = new JSONObject();
            ecommercePrefs.put("loyalty_tier", "Platinum");
            ecommercePrefs.put("coupon_affinity", "high");
            ecommercePrefs.put("coupon_affinity 2", "low");
            ecommercePrefs.put("coupon_affinity 3", "medium");
            ecommercePrefs.put("deep_nested_test_root", currentObject);

            boolean success = brazeUser.setCustomAttribute("predicted_preferences", ecommercePrefs, false);

            if (success) {
                Log.d(TAG, "👑 [JsonObjectAttribute] Không tưởng! Braze đã nuốt gọn gàng 50 tầng dữ liệu.");
            } else {
                Log.e(TAG, "🛑 [JsonObjectAttribute] Trận chiến kết thúc! Chặn đứng hoàn toàn ở tầng thứ 50");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi phát sinh khi lặp dựng 50 tầng JSON: " + e.getMessage());
        }
    }

    private void testSetCustomAttributeJsonArray() {
        try {
            BrazeUser brazeUser = Braze.getInstance(requireContext()).getCurrentUser();
            if (brazeUser == null) return;

            int targetTotalSize = 2;

            JSONArray cartArray = new JSONArray();

            for (int i = 1; i <= targetTotalSize; i++) {

                JSONObject productItem = new JSONObject();
                productItem.put("index", i);
                productItem.put("product_id", "PROD_" + i);

                cartArray.put(productItem);
            }

            boolean success = brazeUser.setCustomAttribute("cart_items", cartArray, false);

            if (success) {
                Log.d(TAG, "🔥 [setCustomAttributeJsonArray] Thành công! Braze chấp nhận Json Array có " + cartArray.length() + " phần tử.");
            } else {
                Log.e(TAG, "🛑 [setCustomAttributeJsonArray] Từ chối! Lỗi khi gọi Json Array có " + cartArray.length() + " phần tử.");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi tạo JSONArray Custom Attribute: " + e.getMessage());
        }
    }

    private void testSetCustomAttributeArray() {
        try {
            BrazeUser brazeUser = Braze.getInstance(requireContext()).getCurrentUser();
            if (brazeUser == null) return;

            int targetTotalSize = 1;

            JSONArray discountCode = new JSONArray();

            for (int i = 1; i <= targetTotalSize; i++) {
                discountCode.put("CODE" + i);
            }

            boolean success = brazeUser.setCustomAttribute("discount_code", discountCode, false);

            if (success) {
                Log.d(TAG, "🔥 [setCustomAttributeArray] Thành công! Braze chấp nhận Array có " + discountCode.length() + " phần tử.");
            } else {
                Log.e(TAG, "🛑 [setCustomAttributeArray] Từ chối! Lỗi khi gọi Array có " + discountCode.length() + " phần tử.");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi tạo JSONArray Custom Attribute: " + e.getMessage());
        }
    }

    private void setupAdapters() {
        String[] countries = {"+84", "+1", "+65", "+60"};
        ArrayAdapter<String> adapterCountry = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, countries) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                v.setBackgroundColor(Color.parseColor("#FFFFFF"));
                return v;
            }
        };
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapterCountry);

        String[] genders = {"Male (M)", "Female (F)", "Other (O)", "Not Applicable (N)", "Prefer not to say (P)", "Unknown (U)"};
        ArrayAdapter<String> adapterGender = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, genders) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                v.setBackgroundColor(Color.parseColor("#FFFFFF"));
                return v;
            }
        };
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapterGender);

        String[] languages = {"Tiếng Việt (vi)", "English (en)", "Chinese (zh)", "Japanese (ja)"};
        ArrayAdapter<String> adapterLang = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, languages) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                v.setBackgroundColor(Color.parseColor("#FFFFFF"));
                return v;
            }
        };
        adapterLang.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapterLang);
    }

    @Override
    public void onResume() {
        super.onResume();
        String globalUserId = MyApplication.getCurrentUserId();

        if (globalUserId != null && !globalUserId.isEmpty()) {
            editCustomUserId.setText(globalUserId);
            editCustomUserId.setSelection(globalUserId.length());
        }
    }

}