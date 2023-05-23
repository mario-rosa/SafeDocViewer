package it.unibas.tesi.mobilereader;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import it.unibas.tesi.mobilereader.controllo.ControlHome;
import it.unibas.tesi.mobilereader.controllo.Encrypt;
import it.unibas.tesi.mobilereader.databinding.ActivityMainBinding;
import it.unibas.tesi.mobilereader.modello.ApiCallBack;
import it.unibas.tesi.mobilereader.vista.FileFragment;
import it.unibas.tesi.mobilereader.vista.HomeFragment;
import it.unibas.tesi.mobilereader.vista.LoginFragment;
import it.unibas.tesi.mobilereader.vista.PdfFragment;
import it.unibas.tesi.mobilereader.vista.WelcomeFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final String[] permissions = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private boolean t = false;
    public boolean isHome = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult( ActivityResult result ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Toast.makeText(MainActivity.this, "Permessi ottenuti", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permessi negati", Toast.LENGTH_SHORT).show();

                        if(t){
                            finish();
                        }
                        t = true;
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Permessi negati", Toast.LENGTH_SHORT).show();

                }
            }
        });
        if (checkPermission()) {
            Toast.makeText(MainActivity.this,"Permessi: ok",Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }
        initStartFragment();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int readCheck = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
            int writeCheck = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
            return readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permessi")
                    .setMessage("Per cominciare è necessario consetire all'app i permessi per leggere, modificare ed eliminare i file sul dispositivo." +
                            "\nCerca 'SecDoc Viewer' nell'elenco e spunta su 'Consenti l'accesso per gestire tutti i file'")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int which ) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                intent.addCategory("android.intent.category.DEFAULT");
                                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                                activityResultLauncher.launch(intent);
                            } catch (Exception e) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                activityResultLauncher.launch(intent);
                            }
                        }
                    })
                    .setCancelable(false)
                    .show();


        } else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 30);
        }
    }
    public void initStartFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, WelcomeFragment.class, null)
                .commit();
    }
    public void goToHomeFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true).replace(R.id.fragment_container_view, HomeFragment.class, null)
                .commit();
    }
    public void goToFileFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true).replace(R.id.fragment_container_view, FileFragment.class, null)
                .commit();
    }
    public void goToPdfFragment(Bundle bundle) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true).replace(R.id.fragment_container_view, PdfFragment.class, bundle)
                .commit();
    }
    public void goToLoginFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true).replace(R.id.fragment_container_view, LoginFragment.class, null)
                .commit();
    }

    public void goToWelcomeFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true).replace(R.id.fragment_container_view, WelcomeFragment.class, null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().findFragmentById(R.id.fragment_container_view) instanceof WelcomeFragment){
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("")
                    .setMessage("Vuoi uscire dall'applicazione?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(Constants.user == null){
                                finish();
                            } else {
                                ControlHome.logout(new ApiCallBack() {
                                    @Override
                                    public void callback(boolean result, String message) {
                                        if (result) {
                                            finish();
                                        }
                                    }

                                });
                            }

                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        } else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container_view) instanceof PdfFragment){
            if(!isHome){
                goToFileFragment();
            } else {
                goToHomeFragment();
            }
        } else {
            goToWelcomeFragment();
        }
    }
}