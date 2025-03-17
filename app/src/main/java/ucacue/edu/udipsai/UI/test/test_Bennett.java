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
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayDeque;

import ucacue.edu.udipsai.R;
import ucacue.edu.udipsai.Services.SerialListener;
import ucacue.edu.udipsai.Services.SerialService;

public class test_Bennett extends AppCompatActivity implements SerialListener, ServiceConnection {
    private SerialService service;
    private StringBuilder fullReceivedData = new StringBuilder(); // Para acumular datos recibidos
    private boolean isBound = false;
    private TextView receivedDataText, tvErrores, tvTiempoEjecucion, tvTituloDatos, tvExtraDato;
    private CardView cardEspera, cardDatos, cardExtraDato;
    private ImageView gifStatusResultado, gifStatusB;
    private ImageButton backButton;
    private FloatingActionButton playButton, resetButton;
    private Button sendButton1, sendButton2, sendButton3;
    private int currentStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_bennett);

        receivedDataText = findViewById(R.id.text_datosB);
        gifStatusB = findViewById(R.id.gif_statusB);
        cardEspera = findViewById(R.id.card_esperaB);
        cardDatos = findViewById(R.id.card_datosB);
        gifStatusResultado = findViewById(R.id.gif_status_resultadoB);
        tvErrores = findViewById(R.id.tv_errores);
        tvTiempoEjecucion = findViewById(R.id.tv_tiempo_ejecucion);
        tvTituloDatos = findViewById(R.id.text_titulo_datosB);
        tvExtraDato = findViewById(R.id.tv_extra_dato);
        cardExtraDato = findViewById(R.id.card_extra_dato);
        sendButton1 = findViewById(R.id.button_enviar_m1B);
        sendButton2 = findViewById(R.id.button_enviar_m2B);
        sendButton3 = findViewById(R.id.button_enviar_m3B);
        backButton = findViewById(R.id.button_regresarB);
        playButton = findViewById(R.id.button_playB);
        resetButton = findViewById(R.id.button_resetB);

        // Inicialmente, el botón "Enviar M1" está deshabilitado y el de reinicio está oculto
        sendButton1.setEnabled(false);
        sendButton2.setEnabled(false);
        sendButton3.setEnabled(false);
        resetButton.setVisibility(View.GONE);

        // Inicialización de valores iniciales
        cardDatos.setVisibility(View.GONE);
        loadGif(gifStatusB, R.drawable.reloj_de_arena);
        receivedDataText.setText("Esperando, presione Comenzar...");
        tvTituloDatos.setText("Esperando datos...");
        tvErrores.setText("-");
        tvTiempoEjecucion.setText("- seg");

        // Iniciar y vincular servicio Bluetooth
        Intent intent = new Intent(this, SerialService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);

        // Botón Play: Habilita "Enviar M1" y muestra "Reinicio"
        playButton.setOnClickListener(v -> {
            sendButton1.setEnabled(true);
            resetButton.setVisibility(View.VISIBLE);
            currentStep = 1; // Estado inicial después de Play
        });

        sendButton1.setOnClickListener(v -> {
            sendData("M1");
            sendButton1.setEnabled(false);
            currentStep = 2; // M1 enviado, esperando respuesta
            loadGif(gifStatusB, R.drawable.dibujo);
            receivedDataText.setText("Ejecutando el Test...");
        });

        sendButton2.setOnClickListener(v -> {
            sendData("M2");
            sendButton2.setEnabled(false);
            currentStep = 4; // M2 enviado, esperando respuesta
            loadGif(gifStatusB, R.drawable.dibujo);
            receivedDataText.setText("Ejecutando el Test...");
        });

        sendButton3.setOnClickListener(v -> {
            sendData("M3");
            sendButton3.setEnabled(false);
            currentStep = 6; // M3 enviado, no hay más pasos
        });

        resetButton.setOnClickListener(v -> {
            sendData("S");
            sendButton1.setEnabled(false);
            sendButton2.setEnabled(false);
            sendButton3.setEnabled(false);
            resetButton.setVisibility(View.GONE);
            receivedDataText.setText("Esperando, presione Comenzar...");
            tvTituloDatos.setText("Esperando datos...");
            tvErrores.setText("-");
            tvTiempoEjecucion.setText("- seg");
            loadGif(gifStatusB, R.drawable.reloj_de_arena);
            cardEspera.setVisibility(View.VISIBLE);
            cardDatos.setVisibility(View.GONE);
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
    private void loadGif(ImageView imageView, int gifResource) {
        Glide.with(this).asGif().load(gifResource).into(imageView);
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
            // Procesa los datos recibidos
            for (byte[] data : datas) {
                try {
                    fullReceivedData.append(new String(data, "UTF-8")); // Acumula los datos recibidos
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Si hay datos recibidos
            if (fullReceivedData.length() > 0) {
                String receivedString = fullReceivedData.toString().trim(); // Convierte los datos a String
                receivedString = receivedString.replace("\n", ","); // Reemplaza saltos de línea por comas
                String[] values = receivedString.split(","); // Divide los valores por comas

                // Si hay al menos 2 valores (tiempo y errores)
                if (values.length >= 2) {
                    // Muestra los datos en la interfaz
                    cardEspera.setVisibility(View.GONE);
                    cardDatos.setVisibility(View.VISIBLE);
                    tvTituloDatos.setText("Resultados del Test");

                    // Muestra el tiempo de ejecución y los errores
                    tvTiempoEjecucion.setText(values[0] + " seg");
                    tvErrores.setText(formatErrors(values[1]));

                    // Si hay un tercer valor (dato extra), lo muestra
                    if (values.length == 3) {
                        tvExtraDato.setText(values[2]);
                        cardExtraDato.setVisibility(View.VISIBLE);
                    } else {
                        cardExtraDato.setVisibility(View.GONE);
                    }

                    // Cambia el GIF a un estado de éxito
                    loadGif(gifStatusResultado, R.drawable.check);

                    // Habilita el siguiente botón según el paso actual
                    switch (currentStep) {
                        case 2: // Respuesta recibida después de M1
                            sendButton2.setEnabled(true); // Habilita el botón 2
                            currentStep = 3; // Actualiza el estado
                            break;
                        case 4: // Respuesta recibida después de M2
                            sendButton3.setEnabled(true); // Habilita el botón 3
                            currentStep = 5; // Actualiza el estado
                            break;
                        case 6: // Respuesta recibida después de M3
                            // No hay más botones que habilitar
                            break;
                    }

                    // Limpia los datos acumulados
                    fullReceivedData.setLength(0);
                }
            }
        });
    }

    /**
     * Método para formatear errores, reemplazando 1 por ❌ y 0 por ✅
     */
    private String formatErrors(String errores) {
        StringBuilder erroresFormatted = new StringBuilder();
        for (char c : errores.toCharArray()) {
            if (c == '1') {
                erroresFormatted.append("❌");
            } else if (c == '0') {
                erroresFormatted.append("✅");
            } else {
                erroresFormatted.append(c);
            }
        }
        return erroresFormatted.toString();
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