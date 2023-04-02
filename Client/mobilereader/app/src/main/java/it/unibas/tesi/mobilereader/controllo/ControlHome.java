package it.unibas.tesi.mobilereader.controllo;

import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.FragmentActivity;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import it.unibas.tesi.mobilereader.Constants;
import it.unibas.tesi.mobilereader.MainActivity;
import it.unibas.tesi.mobilereader.modello.Documento;
import it.unibas.tesi.mobilereader.modello.ApiCallBack;
import okhttp3.Request;
import okhttp3.Response;

public final class ControlHome {

    public static void logout(ApiCallBack apiCallBack){
        AtomicBoolean result = new AtomicBoolean(false);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Request request = new Request.Builder().url(Constants.basicUrl + "/logout").build();
            try {
                Response res = Constants.CLIENT.newCall(request).execute();
                if (res.code() == 200) {
                    result.set(true);
                    res.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                apiCallBack.callback(result.get(), "");
            });
        });
    }
    public static void getPdf(FragmentActivity activity, ApiCallBack apiCallBack, int ref){
        AtomicBoolean result = new AtomicBoolean(false);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Request request = new Request.Builder().url(Constants.basicUrl + "/getPdf/" + ref).build();
            try {
                Response res = Constants.CLIENT.newCall(request).execute();
                if (res.code() == 200) {
                    String decoded = Encrypt.decrypt(res.body().string());
                    JSONObject jsDecoded = new JSONObject(decoded);
                    res.close();
                    try{
                        String path = ControlStorage.savePdfFile((MainActivity)activity,jsDecoded.getString("path").substring(4, jsDecoded.getString("path").indexOf(".pdf")), jsDecoded.getString("pdf"));
                        if(!path.isEmpty()){
                            Date expires = null;
                            if(!jsDecoded.getString("expires").isEmpty()){
                                expires = new Date(Long.parseLong(jsDecoded.getString("expires")));
                                System.out.println(expires);
                            }
                            Documento doc = new Documento(ref, jsDecoded.getString("name"), expires);
                            doc.setPassword(jsDecoded.getString("password"));
                            doc.setPath(path);
                            ControlStorage.savePdfInList(activity, doc);
                            result.set(true);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                apiCallBack.callback(result.get(), "");
            });
        });

    }
    public static void getAllPdf (ApiCallBack apiCallBack) {
        AtomicBoolean result = new AtomicBoolean(false);
        AtomicReference<String> body = new AtomicReference<>("");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Request request = new Request.Builder().url(Constants.basicUrl + "/getAllPdf").build();
            try {
                Response res = Constants.CLIENT.newCall(request).execute();
                if (res.code() == 200) {
                    body.set(res.body().string());
                    result.set(true);
                }
                res.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                apiCallBack.callback(result.get(), body.get());
            });
        });
    }
}
