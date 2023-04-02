package it.unibas.tesi.mobilereader.vista;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.unibas.tesi.mobilereader.Constants;
import it.unibas.tesi.mobilereader.MainActivity;
import it.unibas.tesi.mobilereader.R;
import it.unibas.tesi.mobilereader.controllo.ControlHome;
import it.unibas.tesi.mobilereader.controllo.Encrypt;
import it.unibas.tesi.mobilereader.controllo.Utility;
import it.unibas.tesi.mobilereader.databinding.FragmentHomeBinding;
import it.unibas.tesi.mobilereader.modello.Documento;
import it.unibas.tesi.mobilereader.modello.ApiCallBack;
import it.unibas.tesi.mobilereader.modello.Modello;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView = null;
    private Dialog dialog;
    int webCheckDelay = 2000;
    ExecutorService internetCheckerExec = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        dialog = new Dialog(getContext());
        return binding.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = binding.recyclerDoc;
        super.onViewCreated(view, savedInstanceState);
        if(Modello.getBean("pdfList")==null){
            openDialog("Welcome", "Ciao "+ Constants.user.getUsername() + " in questa schermata è possibile scaricare i documenti.\nClicca su UPDATE per scaricare/aggiornare la lista");
        }else{
            startWebChecker();
        }
        binding.infoview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("Welcome", "Ciao "+ Constants.user.getUsername() + " in questa schermata è possibile scaricare i documenti.\nClicca su UPDATE per scaricare/aggiornare la lista");
            }
        });
        binding.logoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLogoutDialog();
            }
        });
        binding.buttonAggiorna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlHome.getAllPdf(new ApiCallBack() {
                    @Override
                    public void callback(boolean result, String message) {
                        String jsList = Encrypt.decrypt(message);
                        //System.out.println(jsList);
                        List<Documento> pdfList = Utility.parsePdfListFromJson(jsList);
                        Modello.putBean("pdfList", pdfList);
                        if(!pdfList.isEmpty()){
                            setRecyclerView(pdfList);
                        }
                    }
                });
            }
        });
        if(Modello.getBean("pdfList") != null){
            setRecyclerView((List<Documento>)Modello.getBean("pdfList"));
        }
    }

    public void startWebChecker(){
        Handler handler = new Handler(Looper.getMainLooper());
        internetCheckerExec.execute(new Runnable() {
            @Override
            public void run() {
                handler.post(() -> {
                    System.out.println("WebCheckThread in esecuzione!");
                    if(internetCheckerExec.isShutdown()){
                        System.out.println("WebCheckThread terminato!");
                        return;
                    }
                    if(!Utility.isInternetConnected((MainActivity) requireActivity())){
                        new AlertDialog.Builder(getContext()).setTitle("Info").setMessage("Connessione persa!")
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setPositiveButton("Ok", null)
                                .show();
                        Modello.putBean("pdfList", null);
                        Constants.user = null;
                        ((MainActivity)requireActivity()).goToLoginFragment();
                        return;
                    }
                    handler.postDelayed(this, webCheckDelay);
                });
            }
        });
    }

    public void openLogoutDialog(){
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Sei di voler uscire?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ControlHome.logout(new ApiCallBack() {
                            @Override
                            public void callback(boolean result, String message) {
                                if(result){
                                    Toast.makeText(requireContext(), "Logout ok", Toast.LENGTH_SHORT).show();
                                    Modello.putBean("pdfList", null);
                                    Constants.user = null;
                                    ((MainActivity)requireActivity()).goToLoginFragment();
                                }
                            }
                        });
                    }
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public void setRecyclerView (List<Documento> pdfList){
        DocumentAdapter adapter = new DocumentAdapter(pdfList, (AppCompatActivity)getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        internetCheckerExec.shutdownNow();
        binding = null;
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
                startWebChecker();
            }
        });
        dialog.show();
    }

}
