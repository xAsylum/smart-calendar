package com.example.smartcalendar.ui.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.smartcalendar.R;
import com.example.smartcalendar.data.local.TokenManager;
import com.example.smartcalendar.data.models.auth.LoginRequest;
import com.example.smartcalendar.data.models.auth.LoginResponse;
import com.example.smartcalendar.data.network.NetworkClient;
import com.example.smartcalendar.databinding.FragmentLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        String token = TokenManager.getInstance().getToken(getContext());
        if (token != null) {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
        }
        binding.buttonLogin.setOnClickListener(v -> {
            String username = binding.textboxUsername.getText().toString().trim();
            String password = binding.textboxPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Input data", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(username, password);
        });

        return binding.getRoot();

    }

    public void loginUser(String user, String pass) {
        LoginRequest request = new LoginRequest(user, pass);

        NetworkClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getAccessToken();
                    TokenManager.getInstance().saveToken(getContext(), token);
                    Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(LoginFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
                } else {
                    Toast.makeText(getContext(), "Wrong username or password!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("AUTH", "Błąd sieci: " + t.getMessage());
            }
        });
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}