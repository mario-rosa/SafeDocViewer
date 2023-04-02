package it.unibas.tesi.mobilereader.controllo;

import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.os.Environment;
import androidx.fragment.app.FragmentActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import it.unibas.tesi.mobilereader.Constants;
import it.unibas.tesi.mobilereader.MainActivity;
import it.unibas.tesi.mobilereader.modello.Documento;

public class ControlStorage {

    public static void savePdfInList(FragmentActivity activity, Documento doc) {
        SharedPreferences sp = activity.getSharedPreferences("SharedPrefUser" + Constants.user.get_id(), MODE_PRIVATE);
        try {
            List<Documento> pdfList = getAllSavedPdf(activity);
            pdfList.add(doc);
            String str = new Gson().toJson(pdfList);
            JSONArray jsArray = new JSONArray(str);
            SharedPreferences.Editor myEdit = sp.edit();
            myEdit.putString("pdfList", jsArray.toString());
            myEdit.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Documento searchSavedPdf(FragmentActivity activity, int ref) {
        List<Documento> pdfList = getAllSavedPdf(activity);
        for (Documento pdf : pdfList) {
            if (pdf.getId() == ref) {
                return pdf;
            }
        }
        return null;
    }
    public static List<Documento> getAllSavedPdf(FragmentActivity activity) {
        if(Utility.getLastLoginId(activity).isEmpty()){
            return new ArrayList<>();
        }
        SharedPreferences sp = activity.getSharedPreferences("SharedPrefUser" + Utility.getLastLoginId(activity), MODE_PRIVATE);
        List<Documento> pdfList = new ArrayList<>();
        boolean save = false;
        try {
            if (sp.contains("pdfList")) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Documento>>() {}.getType();
                pdfList = gson.fromJson(sp.getString("pdfList", ""), listType);
                for (int i = 0; i < pdfList.size(); i++) {
                    if (!checkSavedPdf(pdfList.get(i))) {
                        pdfList.remove(i);
                        save = true;
                    }
                }
                if (!save) {
                    return pdfList;
                }
            }
            String str = new Gson().toJson(pdfList);
            JSONArray jsArray = new JSONArray(str);
            SharedPreferences.Editor myEdit = sp.edit();
            myEdit.putString("pdfList", jsArray.toString());
            myEdit.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pdfList;
    }

    public static boolean checkSavedPdf(Documento doc) {
        try {
            File file = new File(doc.getPath());
            if (file.exists()) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String savePdfFile(MainActivity activity, String name, String message) {
        byte[] messageDecoded = Base64.getDecoder().decode(message);
        try {
            File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "/." + Constants.user.get_id());
            if (!file.exists()) {
                file.mkdirs();
            }
            File result = new File(file.getAbsolutePath() + File.separator + name + Constants.user.get_id() + ".pdf");
            FileOutputStream fos = new FileOutputStream(result);
            fos.write(messageDecoded);
            fos.close();
            return result.getPath();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
