package kr.ac.hansung.audio.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

public class Permission {

    private Context context;
    private Activity activity;

    private static String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private List<String> permissionList = null;

    private final int MULTIPLE_PERMISSIONS = 1;

    public Permission(FragmentActivity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public boolean checkPermission() {
        permissionList = new ArrayList<>();

        for (String pm : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(context, pm);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }

        if (!permissionList.isEmpty()) {
            return false;
        }

        return true;
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS); // 권한 요청 대화상자 띄우기
    }

    public boolean permissionResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        if(requestCode == MULTIPLE_PERMISSIONS && (grantResults.length > 0)) {
            for (int i=0; i < grantResults.length; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_DENIED) { // 0 : 허용, -1 : 거부
                    return false;
                }
            }
        }

        return true;
    }
}
