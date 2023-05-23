package it.unibas.tesi.mobilereader.vista;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import it.unibas.tesi.mobilereader.Constants;
import it.unibas.tesi.mobilereader.MainActivity;
import it.unibas.tesi.mobilereader.R;
import it.unibas.tesi.mobilereader.controllo.ControlStorage;
import it.unibas.tesi.mobilereader.controllo.ControlHome;
import it.unibas.tesi.mobilereader.modello.ApiCallBack;
import it.unibas.tesi.mobilereader.modello.Documento;
import it.unibas.tesi.mobilereader.modello.Modello;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {
    private List<Documento> listaDocumenti;
    private AppCompatActivity activity;
    boolean downloading = false;
    boolean errorDownlod = false;


    public DocumentAdapter(List<Documento> listaDocumenti, AppCompatActivity activity) {
        this.listaDocumenti = listaDocumenti;
        this.activity = activity;
    }

    @NonNull
    @Override
    public DocumentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_doc, parent, false);

        return new DocumentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentAdapter.ViewHolder holder, int position) {
        int ref = listaDocumenti.get(position).getId();
        holder.textName.setText(listaDocumenti.get(position).getNome());
        holder.textData.setText("");
        if(listaDocumenti.get(position).getScadenza() != null){
            System.out.println(listaDocumenti.get(position).getScadenza());
            if(isScaduto(listaDocumenti.get(position).getScadenza())){
                holder.textData.setText("Documento Scaduto");
                holder.itemView.setEnabled(false);
                ((CardView)holder.itemView.findViewById(R.id.card_view_file)).setCardBackgroundColor(activity.getColor(R.color.gray));

            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                holder.textData.setText("Data Scadenza: " + sdf.format(listaDocumenti.get(position).getScadenza()));
                ((CardView)holder.itemView.findViewById(R.id.card_view_file)).setCardBackgroundColor(activity.getColor(R.color.white));
            }
        }
        if (ControlStorage.searchSavedPdf(activity, ref) != null) {
            holder.itemView.findViewById(R.id.downloadedImageView).setVisibility(View.VISIBLE);
            //holder.itemView.setBackgroundColor(activity.getColor(R.color.gray));
        }else{
            holder.itemView.findViewById(R.id.downloadedImageView).setVisibility(View.INVISIBLE);
            //holder.itemView.setBackgroundColor(activity.getColor(R.color.white));
        }

        holder.itemView.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, ref);
            }
        }));
    }

    private boolean isScaduto(Date scadenza){
        Date today = new Date();
        System.out.println(today.getTime());
        if(today.getTime() > scadenza.getTime()) {
            return true;
        }
        return false;
    }

    private void showPopupMenu(View view, int ref) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.menu);
        if (ControlStorage.searchSavedPdf(activity, ref) != null) {
            popupMenu.getMenu().removeItem(R.id.action_popup_download);
        } else {
            popupMenu.getMenu().removeItem(R.id.action_popup_open);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.action_popup_open){
                    Bundle bundle = new Bundle();
                    bundle.putInt("ref", ref);
                    ((MainActivity)activity).goToPdfFragment(bundle);
                    return true;
                }else if(item.getItemId() == R.id.action_popup_download && !downloading){
                    downloading = true;
                    Toast.makeText(view.getContext(), "Download in corso", Toast.LENGTH_SHORT).show();
                    ControlHome.getPdf(activity, new ApiCallBack() {
                        @Override
                        public void callback(boolean result, String message) {
                            if(result){
                                errorDownlod = false;
                                System.out.println("File salvato in Archivio!");

                            }else{
                                System.out.println("File non salvato!");
                                errorDownlod = true;
                            }
                            downloading = false;
                            if (errorDownlod)
                                openAlertDialog(view.getContext());
                            notifyDataSetChanged();
                        }
                    }, ref);

                    return true;
                }

                return false;
            }
        });
        popupMenu.show();
    }
    public void openAlertDialog(Context context){
        new AlertDialog.Builder(context)
                .setTitle("Errore")
                .setMessage("Download non riuscito")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                // A null listener allows the button to dismiss the dialog and take no further action.

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public int getItemCount() {
        return listaDocumenti.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView textName;
        public TextView textData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview);
            textName = itemView.findViewById(R.id.textViewNameDoc);
            textData = itemView.findViewById(R.id.textViewExpiresDoc);
        }
    }
}
