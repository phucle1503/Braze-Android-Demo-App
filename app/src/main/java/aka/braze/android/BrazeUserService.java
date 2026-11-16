package aka.braze.android;

import android.content.Context;
import android.util.Log;
import com.braze.Braze;
import com.braze.enums.Gender;
import com.braze.enums.Month;

public class BrazeUserService {
    private static final String TAG = "[BrazeUserService]";

    public static void updateUserProfile(
            Context context,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String genderStr,
            String homeCity,
            String country,
            String language
    ) {
        try {
            Braze.getInstance(context).getCurrentUser(currentUser -> {
                if (currentUser == null) {
                    Log.e(TAG, "⚠️ Không thể cập nhật profile: BrazeUser đang null.");
                    return;
                }

                if (firstName != null && !firstName.isEmpty()) currentUser.setFirstName(firstName);
                if (lastName != null && !lastName.isEmpty()) currentUser.setLastName(lastName);
                if (email != null && !email.isEmpty()) currentUser.setEmail(email);
                if (phoneNumber != null && !phoneNumber.isEmpty()) currentUser.setPhoneNumber(phoneNumber);
                if (homeCity != null && !homeCity.isEmpty()) currentUser.setHomeCity(homeCity);
                if (country != null && !country.isEmpty()) currentUser.setCountry(country);
                if (language != null && !language.isEmpty()) currentUser.setLanguage(language);

                if (genderStr != null && !genderStr.isEmpty()) {
                    switch (genderStr.toUpperCase()) {
                        case "M": currentUser.setGender(Gender.MALE); break;
                        case "F": currentUser.setGender(Gender.FEMALE); break;
                        case "O": currentUser.setGender(Gender.OTHER); break;
                        case "N": currentUser.setGender(Gender.NOT_APPLICABLE); break;
                        case "P": currentUser.setGender(Gender.PREFER_NOT_TO_SAY); break;
                        case "U": currentUser.setGender(Gender.UNKNOWN); break;
                        default:
                            Log.w(TAG, "⚠️ Giới tính '" + genderStr + "' không hợp lệ theo quy chuẩn SDK.");
                            break;
                    }
                }
                Log.d(TAG, "✅ Đã đồng bộ default attributes lên Braze profile.");
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi hệ thống khi cập nhật profile: " + e.getMessage());
        }
    }

    public static void setUserDateOfBirth(Context context, int year, int month, int day) {
        try {
            Braze.getInstance(context).getCurrentUser(currentUser -> {
                if (currentUser == null) {
                    Log.e(TAG, "⚠️ Không thể set ngày sinh: BrazeUser đang null.");
                    return;
                }

                Month selectedMonth = Month.getMonth(month);
                if (selectedMonth != null) {
                    currentUser.setDateOfBirth(year, selectedMonth, day);
                    Log.d(TAG, "✅ Set ngày sinh thành công: " + day + "/" + month + "/" + year);
                } else {
                    Log.e(TAG, "⚠️ Giá trị tháng không hợp lệ: " + month);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi setDateOfBirth: " + e.getMessage());
        }
    }
}