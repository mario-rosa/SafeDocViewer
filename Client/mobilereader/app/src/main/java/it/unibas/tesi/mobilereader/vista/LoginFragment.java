package it.unibas.tesi.mobilereader.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import it.unibas.tesi.mobilereader.Constants;
import it.unibas.tesi.mobilereader.MainActivity;

import it.unibas.tesi.mobilereader.R;
import it.unibas.tesi.mobilereader.controllo.ControlLogin;
import it.unibas.tesi.mobilereader.controllo.ControlStorage;
import it.unibas.tesi.mobilereader.controllo.Encrypt;
import it.unibas.tesi.mobilereader.controllo.Utility;
import it.unibas.tesi.mobilereader.databinding.FragmentLoginBinding;
import it.unibas.tesi.mobilereader.modello.ApiCallBack;


public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private boolean request = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.editPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.passview.setBoxStrokeColor(requireActivity().getResources().getColor(R.color.blue));
                binding.wrongPswView.setVisibility(View.INVISIBLE);
            }
        });
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (request || !Utility.isInternetConnected((MainActivity)requireActivity())) { //fix button login
                    return;
                }
                request = true;
                String username = String.valueOf(binding.editUsernameText.getText()).trim();
                String password = Encrypt.md5(String.valueOf(binding.editPasswordText.getText()));
                ControlLogin.login(username, password, new ApiCallBack() {
                    @Override
                    public void callback(boolean result, String message) {
                        if (result) {
                            Toast.makeText(requireContext(), "LOGIN OK", Toast.LENGTH_SHORT).show();
                            Utility.saveLastLoginId(requireActivity());
                            ((MainActivity) requireActivity()).goToHomeFragment();
                            ControlLogin.getKey(new ApiCallBack() {
                                @Override
                                public void callback(boolean result, String message) {
                                    request = false;
                                    if (result) {
                                        System.out.println("Message and Key OK");
                                    }
                                }
                            });
                        } else {
                            request = false;
                            binding.passview.setBoxStrokeColor(requireActivity().getResources().getColor(R.color.red));
                            binding.wrongPswView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}

