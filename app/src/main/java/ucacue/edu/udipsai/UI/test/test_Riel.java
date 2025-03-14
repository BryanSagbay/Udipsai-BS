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

public class test_Riel extends AppCompatActivity implements SerialListener, ServiceConnection {
    private SerialService service;
    private TextView receivedDataText;
    private StringBuilder fullReceivedData = new StringBuilder();
    private boolean isBound = false;

    private ImageView gifStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_riel);

        receivedDataText = findViewById(R.id.text_datosR);
        gifStatus = findViewById(R.id.gif_statusR);
        Button sendButton = findViewById(R.id.button_enviar_m1R);
        ImageButton backButton = findViewById(R.id.button_regresarR);
        FloatingActionButton playButton = findViewById(R.id.button_play);
        FloatingActionButton resetButton = findViewById(R.id.button_reset);

        // Inicialmente, el botón "Enviar M1" está deshabilitado y el de reinicio está oculto
        sendButton.setEnabled(false);
        resetButton.setVisibility(View.GONE);

        // Cargar GIF de espera al inicio
        loadGif(R.drawable.reloj_de_arena);
        receivedDataText.setText("Esperando, presione Comenzar..");

        // Iniciar y vincular servicio Bluetooth
        Intent intent = new Intent(this, SerialService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);

        // Botón Play: Habilita "Enviar M1" y muestra "Reinicio"
        playButton.setOnClickListener(v -> {
            sendButton.setEnabled(true); // Habilitar botón Enviar M1
            resetButton.setVisibility(View.VISIBLE); // Mostrar botón de reinicio
        });

        // Botón Enviar M1: Enviar comando y deshabilitar
        sendButton.setOnClickListener(v -> {
            sendData("M1");
            sendButton.setEnabled(false);
            loadGif(R.drawable.dibujo);
            receivedDataText.setText("Ejecutando el juego...");
        });

        // Botón de reinicio: Enviar comando "S" y limpiar datos
        resetButton.setOnClickListener(v -> {
            sendData("S"); // Enviar el comando de reinicio
            receivedDataText.setText("Esperando datos...");
            sendButton.setEnabled(false);
            resetButton.setVisibility(View.GONE);
            loadGif(R.drawable.reloj_de_arena);

        });

        // Botón para regresar y desconectar Bluetooth
        backButton.setOnClickListener(v -> {
            disconnectBluetooth();
            Intent homeIntent = new Intent(test_Riel.this, HomeTest.class);
            startActivity(homeIntent);
            finish();
        });
    }


    private void loadGif(int gifResource) {
        Glide.with(this)
                .asGif()
                .load(gifResource)
                .into(gifStatus);
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

            // Procesar los datos recibidos
            if (fullReceivedData.length() > 0) {
                String receivedString = fullReceivedData.toString().trim();

                // Dividir los datos usando la coma como separador
                String[] values = receivedString.split(",");

                if (values.length == 2) { // Asegurar que se reciban dos valores
                    String errores = values[0]; // Primer valor (errores)
                    String tiempoEjecucion = values[1]; // Segundo valor (tiempo de ejecución)

                    // Formatear la salida deseada
                    String formattedText = "Errores:\n" + errores + "\nTiempo de Ejecución: \n" + tiempoEjecucion;

                    // Mostrar los datos formateados en el TextView
                    if (receivedDataText != null) {
                        receivedDataText.setText(formattedText);
                    }

                    // Mostrar GIF de línea de meta
                    loadGif(R.drawable.linea_de_meta);

                    // Limpiar el StringBuilder después de procesar los datos
                    fullReceivedData.setLength(0);
                }
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