package com.gurzelai.flashsos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    boolean flashEncendido = false;
    boolean flashAccesible = false;
    private CameraManager mCameraManager;
    private String mCameraId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pantallaCompleta();
        inicializar();
        FloatingActionButton btn = findViewById(R.id.boton);
        btn.setOnClickListener(v -> flash());
    }

    private void flash() {
        String morse = "...---...";
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < morse.length(); i++) {
                    Character c = morse.charAt(i);
                    actualizarFlash();
                    try {
                        if (c.equals('-')) {
                            Thread.sleep(1300);
                        }
                        if (c.equals('.')) {
                            Thread.sleep(600, 500);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    actualizarFlash();
                }
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
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void inicializar() {
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        comprobarAccesibilidad();
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

    public void pantallaCompleta(){
        getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}