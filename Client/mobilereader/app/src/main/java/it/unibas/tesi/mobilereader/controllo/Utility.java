package it.unibas.tesi.mobilereader.controllo;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import androidx.fragment.app.FragmentActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import it.unibas.tesi.mobilereader.Constants;
import it.unibas.tesi.mobilereader.MainActivity;
import it.unibas.tesi.mobilereader.modello.Documento;

public class Utility {

    public static String getLastLoginId(FragmentActivity activity){
        SharedPreferences sp = activity.getSharedPreferences("SharedPrefApp", MODE_PRIVATE);
        return sp.getString("lastUserId", "");
    }

    public static void saveLastLoginId(FragmentActivity activity){
        SharedPreferences sp = activity.getSharedPreferences("SharedPrefApp", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sp.edit();
        myEdit.putString("lastUserId", Constants.user.get_id());
        myEdit.apply();
    }
    public static List<Documento> parsePdfListFromJson(String js){
        List<Documento> documents = new ArrayList<>();
        try {
            JSONArray stringArray = new JSONArray(js);
            for(int i = 0; i < stringArray.length(); i++){
                JSONObject jobj = stringArray.getJSONObject(i);
                documents.add(new Documento(jobj.getInt("ref"), jobj.getString("name"), null));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return documents;
    }

    public static boolean isInternetConnected(MainActivity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetwork() != null && cm.getNetworkCapabilities(cm.getActiveNetwork()) != null;
    }
}
