package ucacue.edu.udipsai.UI.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayDeque;

import ucacue.edu.udipsai.R;
import ucacue.edu.udipsai.Services.SerialListener;
import ucacue.edu.udipsai.Services.SerialService;

public class test_Bennett extends AppCompatActivity implements SerialListener, ServiceConnection {
    private SerialService service;
    private TextView receivedDataText;
    private StringBuilder fullReceivedData = new StringBuilder(); // Para acumular datos recibidos
    private boolean isBound = false;
    private ImageView gifStatusB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_bennett);

        receivedDataText = findViewById(R.id.text_datosB);
        gifStatusB = findViewById(R.id.gif_statusB);
        Button sendButton1 = findViewById(R.id.button_enviar_m1B);
        Button sendButton2 = findViewById(R.id.button_enviar_m2B);
        Button sendButton3 = findViewById(R.id.button_enviar_m3B);
        ImageButton backButton = findViewById(R.id.button_regresarB);
        FloatingActionButton playButton = findViewById(R.id.button_playB);
        FloatingActionButton resetButton = findViewById(R.id.button_resetB);

        // Inicialmente, el botón "Enviar M1" está deshabilitado y el de reinicio está oculto
        sendButton1.setEnabled(false);
        resetButton.setVisibility(View.GONE);

        sendButton2.setEnabled(false);
        resetButton.setVisibility(View.GONE);

        sendButton3.setEnabled(false);
        resetButton.setVisibility(View.GONE);

        // Cargar GIF de espera al inicio
        loadGif(R.drawable.reloj_de_arena);
        receivedDataText.setText("Esperando, presione Comenzar..");

        // Iniciar y vincular servicio Bluetooth
        Intent intent = new Intent(this, SerialService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);

        // Botón Play: Habilita "Enviar M1" y muestra "Reinicio"
        playButton.setOnClickListener(v -> {
            sendButton1.setEnabled(true);
            sendButton2.setEnabled(true);
            sendButton3.setEnabled(true);
            resetButton.setVisibility(View.VISIBLE);
        });

        // Botón Enviar M1: Enviar comando y deshabilitar
        sendButton1.setOnClickListener(v -> {
            sendData("M1");
            sendButton1.setEnabled(false);
            sendButton2.setEnabled(false);
            sendButton3.setEnabled(false);
            loadGif(R.drawable.dibujo);
            receivedDataText.setText("Ejecutando el juego...");
        });

        sendButton2.setOnClickListener(v -> {
            sendData("M2");
            sendButton1.setEnabled(false);
            sendButton2.setEnabled(false);
            sendButton3.setEnabled(false);
            loadGif(R.drawable.dibujo);
            receivedDataText.setText("Ejecutando el juego...");
        });

        sendButton3.setOnClickListener(v -> {
            sendData("M3");
            sendButton1.setEnabled(false);
            sendButton2.setEnabled(false);
            sendButton3.setEnabled(false);
            loadGif(R.drawable.dibujo);
            receivedDataText.setText("Ejecutando el juego...");
        });

        // Botón de reinicio: Enviar comando "S" y limpiar datos
        resetButton.setOnClickListener(v -> {
            sendData("S"); // Enviar el comando de reinicio
            receivedDataText.setText("Esperando datos...");
            sendButton1.setEnabled(false);
            sendButton2.setEnabled(false);
            sendButton3.setEnabled(false);
            resetButton.setVisibility(View.GONE);
            loadGif(R.drawable.reloj_de_arena);

        });

        // Botón para regresar y desconectar Bluetooth
        backButton.setOnClickListener(v -> {
            disconnectBluetooth();
            Intent homeIntent = new Intent(test_Bennett.this, HomeTest.class);
            startActivity(homeIntent);
            finish();
        });
    }

    // Cargar GIFs
    private void loadGif(int gifResource) {
        Glide.with(this)
                .asGif()
                .load(gifResource)
                .into(gifStatusB);
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
            loadGif(R.drawable.linea_de_meta);
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
