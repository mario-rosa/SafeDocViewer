package it.unibas.tesi.mobilereader.vista;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.List;

import it.unibas.tesi.mobilereader.R;
import it.unibas.tesi.mobilereader.controllo.ControlStorage;
import it.unibas.tesi.mobilereader.databinding.FragmentFileBinding;
import it.unibas.tesi.mobilereader.modello.Documento;
import it.unibas.tesi.mobilereader.modello.Modello;

public class FileFragment extends Fragment {
    private FragmentFileBinding binding;
    private Dialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFileBinding.inflate(inflater, container, false);
        dialog = new Dialog(getContext());
        return binding.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.infoview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("Archivo", "In questa schermata è possibile visualizzare i file che hai scaricato");
            }
        });
        if (ControlStorage.getAllSavedPdf(requireActivity())!= null){
            if(Modello.getBean("fileDialog") == null) {
                openDialog("Archivo", "In questa schermata è possibile visualizzare i file che hai scaricato");
            }
        }
        setRecyclerView(ControlStorage.getAllSavedPdf(requireActivity()));

    }

    public void setRecyclerView (List<Documento> pdfList){
        DocumentAdapter adapter = new DocumentAdapter(pdfList, (AppCompatActivity)getActivity());
        binding.recyclerDocFileFrag.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerDocFileFrag.setAdapter(adapter);
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
                Modello.putBean("fileDialog", true);
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
