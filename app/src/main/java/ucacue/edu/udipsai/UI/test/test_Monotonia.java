package ucacue.edu.udipsai.UI.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayDeque;

import ucacue.edu.udipsai.R;
import ucacue.edu.udipsai.Services.SerialListener;
import ucacue.edu.udipsai.Services.SerialService;

public class test_Monotonia extends AppCompatActivity implements SerialListener, ServiceConnection {
    private SerialService service;
    private TextView receivedDataText;
    private StringBuilder fullReceivedData = new StringBuilder(); // Para acumular datos recibidos
    private boolean isBound = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_monotonia);

        receivedDataText = findViewById(R.id.text_datos);
        Button sendButtonS1 = findViewById(R.id.button_aletorio);
        Button sendButtonS2 = findViewById(R.id.button_horario);
        Button sendButtonS3 = findViewById(R.id.button_Antihorario);
        Button sendButton1 = findViewById(R.id.button_rojo);
        Button sendButton2 = findViewById(R.id.button_amarillo);
        Button sendButton3 = findViewById(R.id.button_azul);
        Button sendButton4 = findViewById(R.id.button_verde);
        Button backButton = findViewById(R.id.button_regresar);

        // Botón flotante Play
        FloatingActionButton fabPlay = findViewById(R.id.fab_play);
        FloatingActionButton fabReset = findViewById(R.id.fab_reset);

        // Iniciar y vincular servicio Bluetooth
        Intent intent = new Intent(this, SerialService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);

        // Botón para habilitar los botones
        fabPlay.setOnClickListener(v -> {
            habilitarBotones();
            fabReset.setVisibility(View.VISIBLE); // Mostrar botón reset
            Toast.makeText(this, "Botones habilitados", Toast.LENGTH_SHORT).show();
        });

        // Botón para resetear y deshabilitar botones
        fabReset.setOnClickListener(v -> {
            deshabilitarBotones();
            fabReset.setVisibility(View.GONE); // Ocultar botón reset
            Toast.makeText(this, "Botones deshabilitados", Toast.LENGTH_SHORT).show();
        });

        // Botones de envío de datos
        sendButtonS1.setOnClickListener(v -> sendData("M1"));
        sendButtonS2.setOnClickListener(v -> sendData("M2"));
        sendButtonS3.setOnClickListener(v -> sendData("M3"));
        sendButton1.setOnClickListener(v -> sendData("rojo"));
        sendButton2.setOnClickListener(v -> sendData("amarillo"));
        sendButton3.setOnClickListener(v -> sendData("azul"));
        sendButton4.setOnClickListener(v -> sendData("verde"));

        // Botón para regresar y desconectar Bluetooth
        backButton.setOnClickListener(v -> {
            disconnectBluetooth();
            Intent homeIntent = new Intent(test_Monotonia.this, HomeTest.class);
            startActivity(homeIntent);
            finish();
        });

        // Deshabilitar botones al inicio
        deshabilitarBotones();
    }

    /**
     * Método para habilitar los botones
     */
    private void habilitarBotones() {
        Button[] botones = {
                findViewById(R.id.button_aletorio),
                findViewById(R.id.button_horario),
                findViewById(R.id.button_Antihorario),
                findViewById(R.id.button_rojo),
                findViewById(R.id.button_amarillo),
                findViewById(R.id.button_azul),
                findViewById(R.id.button_verde)
        };

        for (Button btn : botones) {
            btn.setEnabled(true);
            btn.setAlpha(1.0f); // Quitar opacidad
        }
    }

    /**
     * Método para deshabilitar los botones
     */
    private void deshabilitarBotones() {
        Button[] botones = {
                findViewById(R.id.button_aletorio),
                findViewById(R.id.button_horario),
                findViewById(R.id.button_Antihorario),
                findViewById(R.id.button_rojo),
                findViewById(R.id.button_amarillo),
                findViewById(R.id.button_azul),
                findViewById(R.id.button_verde)
        };

        for (Button btn : botones) {
            btn.setEnabled(false);
            btn.setAlpha(0.5f); // Poner opacidad para indicar que están deshabilitados
        }
    }


    /**
     * Enviar datos al dispositivo Bluetooth
     */
    private void sendData(String message) {
        if (service != null) {
            try {
                service.write(message.getBytes("UTF-8")); // Compatible con API 18+
                Toast.makeText(this, "Mensaje enviado: " + message, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Error al enviar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No hay conexión Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Recibir y mostrar datos del dispositivo Bluetooth
     */
    @Override
    public void onSerialRead(ArrayDeque<byte[]> datas) {
        runOnUiThread(() -> {
            for (byte[] data : datas) {
                try {
                    fullReceivedData.append(new String(data, "UTF-8")); // Acumular los datos recibidos
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Mostrar los datos completos en el TextView
            if (receivedDataText != null) {
                receivedDataText.setText("Datos recibidos: " + fullReceivedData.toString());
            }
        });
    }

    /**
     * Manejo de conexión y errores Bluetooth
     */
    @Override
    public void onSerialConnect() {
        runOnUiThread(() -> Toast.makeText(this, "Conexión Bluetooth establecida", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onSerialConnectError(Exception e) {
        runOnUiThread(() -> Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onSerialRead(byte[] data) {
    }

    @Override
    public void onSerialIoError(Exception e) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error de comunicación", Toast.LENGTH_SHORT).show();
            disconnectBluetooth();
        });
    }

    /**
     * Cerrar conexión Bluetooth
     */
    private void disconnectBluetooth() {
        if (service != null) {
            service.disconnect();
        }
        if (isBound) {
            unbindService(this);
            isBound = false;
        }
    }

    /**
     * Métodos para el ServiceConnection
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        isBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        isBound = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectBluetooth();
    }
}
