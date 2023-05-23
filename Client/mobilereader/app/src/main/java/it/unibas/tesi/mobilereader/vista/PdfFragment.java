package it.unibas.tesi.mobilereader.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.util.Date;
import it.unibas.tesi.mobilereader.MainActivity;
import it.unibas.tesi.mobilereader.controllo.ControlStorage;
import it.unibas.tesi.mobilereader.databinding.FragmentPdfBinding;
import it.unibas.tesi.mobilereader.modello.Documento;

public class PdfFragment extends Fragment {

    private FragmentPdfBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPdfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPdf(ControlStorage.searchSavedPdf(requireActivity(), this.getArguments().getInt("ref")));
    }

    public void setPdf(Documento pdf) {
        if(pdf.getScadenza() != null){
            Date today = new Date();
            if(today.getTime() > pdf.getScadenza().getTime()){
                Toast.makeText(requireContext(), "Documento scaduto!", Toast.LENGTH_SHORT).show();
                ((MainActivity)requireActivity()).goToHomeFragment();
                return;
            }
        }
        File result = new File(pdf.getPath());
        binding.pdfView.fromFile(result).password(pdf.getPassword()).autoSpacing(false).load();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
