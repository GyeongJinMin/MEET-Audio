package kr.ac.hansung.audio.main;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import kr.ac.hansung.audio.R;
import kr.ac.hansung.audio.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding = null;

    private MainViewModel mainViewModel;

    private Permission permission;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding.btnSubmit.setOnClickListener(view -> {
            mainViewModel.setTopic(binding.etRoomID.getText().toString());
            mainViewModel.setName(binding.etName.getText().toString());

            mainViewModel.clickSubmit();

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_mainFragment_to_callingFragment);
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        permissionCheck();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void permissionCheck() {
        // SDK 23버전 이하 버전에서는 Permission이 필요하지 않음
        if(Build.VERSION.SDK_INT >= 23) {
            permission = new Permission(getActivity(), requireContext());

            if(!permission.checkPermission()) {
                permission.requestPermission();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 권한 허용을 거부한 경우, 재요청
        if(!permission.permissionResult(requestCode, permissions, grantResults)) {
            permission.requestPermission();
        }
    }
}