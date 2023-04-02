package it.unibas.tesi.mobilereader.vista;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import it.unibas.tesi.mobilereader.Constants;
import it.unibas.tesi.mobilereader.MainActivity;
import it.unibas.tesi.mobilereader.R;
import it.unibas.tesi.mobilereader.databinding.FragmentHomeBinding;
import it.unibas.tesi.mobilereader.databinding.FragmentWelcomeBinding;
import it.unibas.tesi.mobilereader.modello.Modello;

public class WelcomeFragment extends Fragment {

    private FragmentWelcomeBinding binding;
    private Dialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        dialog = new Dialog(getContext());
        return binding.getRoot();

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.infoview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("Benvenuto!", "SafeDoc Viewer è un'app per scaricare e visualizzare documenti PDF in modo protetto.\n" +
                        "App sviluppata da Mario Rosa come progetto di tesi Laurea triennale in Informatica presso all'Università degli Studi della Basilicata");
            }
        });
        if(Constants.user != null){
            binding.constraintLoginImage.setBackground(requireActivity().getDrawable(R.drawable.home));
            binding.infoview.setVisibility(View.INVISIBLE);
            binding.textLogin.setText("Home");
        }else{
            binding.constraintLoginImage.setBackground(requireActivity().getDrawable(R.drawable.login));
            binding.infoview.setVisibility(View.VISIBLE);
            binding.textLogin.setText("Log in");
        }
        binding.loginWelcomeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Constants.user != null){
                    ((MainActivity)requireActivity()).isHome = true;
                    ((MainActivity)requireActivity()).goToHomeFragment();
                }else{
                    ((MainActivity)requireActivity()).goToLoginFragment();
                }
            }
        });
        binding.fileWelcomeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)requireActivity()).isHome = false;
                ((MainActivity)requireActivity()).goToFileFragment();
            }
        });
    }

    private void openDialog(String titolo, String testo){
        dialog.setContentView(R.layout.dialog_custom);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        TextView text1 = (TextView) dialog.findViewById(R.id.info_title);
        text1.setText(titolo);
        TextView text2 = (TextView) dialog.findViewById(R.id.info_message);
        text2.setText(testo);
        Button buttonOk = dialog.findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
