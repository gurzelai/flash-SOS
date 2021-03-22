package com.gurzelai.flashsos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    // ID BANNER ca-app-pub-3237439786100647/6160539854

    boolean flashEncendido = false;
    boolean flashAccesible = false;
    boolean vibradoActivado;
    boolean sonidoActivado;
    private CameraManager mCameraManager;
    private String mCameraId;
    Vibrator v;
    ToneGenerator toneG;

    boolean loop;

    SeekBar seekBar;
    TextView tvVelocidad;
    double velocidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializarCamaraYVibrador();
        inicializarBanner();
        FloatingActionButton btn = findViewById(R.id.boton);
        btn.setOnClickListener(v -> flash());
        loop = false;
        vibradoActivado = false;
        sonidoActivado = false;
        velocidad = 1;
        iniSwitchMaterial();
        tvVelocidad = findViewById(R.id.velocidad);
        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    //hace un llamado a la perilla cuando se arrastra
                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        tvVelocidad.setText("X" + (double) progress / (double) 50);
                        velocidad = (double) progress / 50;

                    }

                    //hace un llamado  cuando se toca la perilla
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    //hace un llamado  cuando se detiene la perilla
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
    }

    private void iniSwitchMaterial() {
        SwitchMaterial sw = findViewById(R.id.loop);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loop = isChecked;
            }
        });
        SwitchMaterial swV = findViewById(R.id.vibracion);
        swV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vibradoActivado = isChecked;
            }
        });
        SwitchMaterial swS = findViewById(R.id.sonido);
        swS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sonidoActivado = isChecked;
                if(isChecked) Toast.makeText(getApplicationContext(), "Volumen: MAX", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inicializarBanner() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void flash() {
        String morse = "...---...";
        new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    for (int i = 0; i < morse.length(); i++) {
                        Character c = morse.charAt(i);
                        actualizarFlash();
                        try {
                            if (c.equals('-')) {
                                Thread.sleep((long) (1300 / velocidad));
                            }
                            if (c.equals('.')) {
                                Thread.sleep((long) (600 / velocidad), (int) (600 / velocidad));
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        actualizarFlash();
                    }
                } while (loop);
            }
        }).start();
    }

    private void actualizarFlash() {
        try {
            if (flashEncendido) {
                mCameraManager.setTorchMode(mCameraId, false);
                flashEncendido = false;
            } else {
                try {
                    mCameraManager.setTorchMode(mCameraId, true);
                    flashEncendido = true;
                    if (vibradoActivado) v.vibrate((long) (400 / velocidad));
                    if (sonidoActivado)
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, (int) (70 / velocidad));
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void inicializarCamaraYVibrador() {
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        comprobarAccesibilidad();
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    }

    private void comprobarAccesibilidad() {
        flashAccesible = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!flashAccesible) {
            showNoFlashError();
        }
    }

    private void showNoFlashError() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .create();
        alert.setTitle("Oops!");
        alert.setMessage("No se puede acceder al flash...");
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.show();
    }

    public void pantallaCompleta() {
        getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pantallaCompleta();
    }

}