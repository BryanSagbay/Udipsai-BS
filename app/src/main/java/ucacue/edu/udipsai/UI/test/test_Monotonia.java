package ucacue.edu.udipsai.UI.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class test_Monotonia extends AppCompatActivity implements SerialListener, ServiceConnection {
    private SerialService service;
    private boolean isBound = false;

    // Elementos de la UI
    private TextView receivedDataText, tvAciertos, tvErrores, tvTiempoEjecucion, tvTiempoReaccion, tvTituloDatos;
    private ImageView gifStatusM, gifStatusResultado;
    private CardView cardEspera, cardDatosM;
    private Spinner spinnerOptionsM;
    private StringBuilder fullReceivedData = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_monotonia);

        cardEspera = findViewById(R.id.card_espera);
        cardDatosM = findViewById(R.id.card_datosM);
        receivedDataText = findViewById(R.id.text_datosM);
        gifStatusM = findViewById(R.id.gif_statusM);
        gifStatusResultado = findViewById(R.id.gif_status_resultado);
        tvAciertos = findViewById(R.id.tv_aciertos);
        tvErrores = findViewById(R.id.tv_errores);
        tvTiempoEjecucion = findViewById(R.id.tv_tiempo_ejecucion);
        tvTiempoReaccion = findViewById(R.id.tv_tiempo_reaccion);
        tvTituloDatos = findViewById(R.id.text_titulo_datosM);
        gifStatusM = findViewById(R.id.gif_statusM);
        spinnerOptionsM = findViewById(R.id.spinner_optionsM);
        Button sendButton1 = findViewById(R.id.button_rojoM);
        Button sendButton2 = findViewById(R.id.button_amarilloM);
        Button sendButton3 = findViewById(R.id.button_azulM);
        Button sendButton4 = findViewById(R.id.button_verdeM);
        ImageButton backButton = findViewById(R.id.button_regresarM);
        FloatingActionButton playButton = findViewById(R.id.button_playM);
        FloatingActionButton resetButton = findViewById(R.id.button_resetM);

        // Configurar Spinner con opciones
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptionsM.setAdapter(adapter);

        spinnerOptionsM.setSelection(0);

        // Inicialmente, deshabilitar el Spinner y los botones
        spinnerOptionsM.setEnabled(false);
        sendButton1.setEnabled(false);
        sendButton2.setEnabled(false);
        sendButton3.setEnabled(false);
        sendButton4.setEnabled(false);
        resetButton.setVisibility(View.GONE);

        // Inicialización de valores iniciales
        cardDatosM.setVisibility(View.GONE);
        loadGif(gifStatusM, R.drawable.reloj_de_arena);
        receivedDataText.setText("Esperando, presione Comenzar...");
        tvTituloDatos.setText("Esperando datos...");
        tvAciertos.setText("-");
        tvErrores.setText("-");
        tvTiempoEjecucion.setText("- seg");
        tvTiempoReaccion.setText("- seg");

        // Iniciar y vincular servicio Bluetooth
        Intent intent = new Intent(this, SerialService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);

        // Botón Play: Habilita el Spinner
        playButton.setOnClickListener(v -> {
            spinnerOptionsM.setEnabled(true);
            resetButton.setVisibility(View.VISIBLE);
        });

        // Configurar el evento de selección del Spinner
        spinnerOptionsM.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();

                // Habilitar los botones al seleccionar una opción válida
                if (!selectedOption.equals("Seleccionar")) {
                    sendButton1.setEnabled(true);
                    sendButton2.setEnabled(true);
                    sendButton3.setEnabled(true);
                    sendButton4.setEnabled(true);
                    spinnerOptionsM.setEnabled(false);
                }

                switch (selectedOption) {
                    case "Aleatorio":
                        sendData("M1");
                        break;
                    case "Horario":
                        sendData("M2");
                        break;
                    case "Antihorario":
                        sendData("M3");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Configurar los eventos de los botones para deshabilitarlos tras ser presionados
        View.OnClickListener buttonClickListener = v -> {
            sendButton1.setEnabled(false);
            sendButton2.setEnabled(false);
            sendButton3.setEnabled(false);
            sendButton4.setEnabled(false);
            loadGif(gifStatusM, R.drawable.dibujo);
            receivedDataText.setText("Ejecutando el test...");
        };

        sendButton1.setOnClickListener(v -> {
            sendData("rojo");
            buttonClickListener.onClick(v);
        });

        sendButton2.setOnClickListener(v -> {
            sendData("amarillo");
            buttonClickListener.onClick(v);
        });

        sendButton3.setOnClickListener(v -> {
            sendData("azul");
            buttonClickListener.onClick(v);
        });

        sendButton4.setOnClickListener(v -> {
            sendData("verde");
            buttonClickListener.onClick(v);
        });

        // Botón de reinicio: Enviar comando "S" y limpiar datos
        resetButton.setOnClickListener(v -> {
            sendData("S");
            sendButton1.setEnabled(false);
            sendButton2.setEnabled(false);
            sendButton3.setEnabled(false);
            sendButton4.setEnabled(false);
            spinnerOptionsM.setSelection(0);
            spinnerOptionsM.setEnabled(false);
            tvTituloDatos.setText("Esperando datos...");
            tvAciertos.setText("-");
            tvErrores.setText("-");
            tvTiempoEjecucion.setText("- seg");
            tvTiempoReaccion.setText("- seg");
            resetButton.setVisibility(View.GONE);
            loadGif(gifStatusM, R.drawable.reloj_de_arena);
            cardEspera.setVisibility(View.VISIBLE);
            cardDatosM.setVisibility(View.GONE);
        });

        // Botón para regresar y desconectar Bluetooth
        backButton.setOnClickListener(v -> {
            disconnectBluetooth();
            Intent homeIntent = new Intent(test_Monotonia.this, HomeTest.class);
            startActivity(homeIntent);
            finish();
            Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show();
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
            for (byte[] data : datas) {
                try {
                    fullReceivedData.append(new String(data, "UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fullReceivedData.length() > 0) {
                String receivedString = fullReceivedData.toString().trim();
                String[] values = receivedString.split(",");

                if (values.length == 4) {
                    String acierto = values[0];
                    String errores = values[1];
                    String tiempoEjecucion = values[2];
                    String tiempoReaccion = values[3];

                    // Ocultar la card de espera y mostrar la de resultados
                    cardEspera.setVisibility(View.GONE);
                    cardDatosM.setVisibility(View.VISIBLE);

                    // Actualizar los datos
                    tvTituloDatos.setText("Resultados del Test");
                    tvAciertos.setText(acierto);
                    tvErrores.setText(errores);
                    tvTiempoEjecucion.setText(tiempoEjecucion + " seg");
                    tvTiempoReaccion.setText(tiempoReaccion + " seg");

                    loadGif(gifStatusResultado, R.drawable.linea_de_meta);
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
