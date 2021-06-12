package kr.ac.hansung.audio;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.List;

import lombok.Setter;

public class MainActivity extends AppCompatActivity {

//    public interface OnBackPressedListener {
//        void onBackPressed();
//    }
//    private OnBackPressedListener onBackPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

//    @Override
//    public void onBackPressed() {
//        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
//        if (fragmentList != null) {
//            for(Fragment fragment : fragmentList) {
//                if(fragment instanceof OnBackPressedListener) {
//                    ((OnBackPressedListener)fragment).onBackPressed();
//                }
//            }
//        }
//
//        if (onBackPressedListener != null) {
//            onBackPressedListener.onBackPressed();
//        } else {
//            AlertDialog alertDialog = new AlertDialog.Builder(this)
//                    .setTitle("종료")
//                    .setMessage("앱을 종료하시겠습니까?")
//                    .setPositiveButton(R.string.ok, (dialog, which) -> {
//                        MainActivity.super.onBackPressed();
//                    })
//                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
//
//                    })
//                    .create();
//
//            alertDialog.show();
//        }
//    }
}