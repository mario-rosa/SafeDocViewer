package it.unibas.tesi.mobilereader.controllo;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import it.unibas.tesi.mobilereader.Constants;
import it.unibas.tesi.mobilereader.modello.ApiCallBack;
import it.unibas.tesi.mobilereader.modello.Modello;
import it.unibas.tesi.mobilereader.modello.User;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class ControlLogin {
    public static void getKey(ApiCallBack apiCallBack) {
        AtomicBoolean result = new AtomicBoolean(false);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            RequestBody formBody = new FormBody.Builder().add("clientPublicKey", (String) Modello.getBean("clientPublicKeyPEM")).build();
            Request request = new Request.Builder().url(Constants.basicUrl + "/getKey").post(formBody).build();
            try {
                Response res = Constants.CLIENT.newCall(request).execute();
                if (res.code() == 200) {
                    JSONObject jsRes = new JSONObject(res.body().string());
                    String jsonAsyncKey = Encrypt.decryptMessage(jsRes.getString("message"), jsRes.getString("publicKey"));
                    JSONObject jsKeys = new JSONObject(jsonAsyncKey);
                    Modello.putBean("securityKey", jsKeys.getString("securitykey"));
                    Modello.putBean("initVector", jsKeys.getString("initVector"));

                    result.set(true);
                }
                res.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                apiCallBack.callback(result.get(), "");
            });
        });
    }
    public static void login(String user, String password, ApiCallBack apiCallBack){
        AtomicBoolean result = new AtomicBoolean(false);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            RequestBody formBody = new FormBody.Builder().add("username", user).add("password", password).build();
            Request request = new Request.Builder().url(Constants.basicUrl+"/login").post(formBody).build();
            try {
                Response res = Constants.CLIENT.newCall(request).execute();
                if(res.code() == 200){
                    Gson gson = new Gson();
                    Constants.user = gson.fromJson(res.body().string(), User.class);
                    result.set(true);
                }
                res.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                apiCallBack.callback(result.get(), "");
            });
        });
    }
}
