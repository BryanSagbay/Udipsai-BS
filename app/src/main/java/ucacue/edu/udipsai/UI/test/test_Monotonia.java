package ucacue.edu.udipsai.UI.test;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayDeque;

import ucacue.edu.udipsai.R;
import ucacue.edu.udipsai.Services.SerialListener;
import ucacue.edu.udipsai.Services.SerialService;

public class test_Monotonia extends AppCompatActivity implements SerialListener, ServiceConnection {

    private FloatingActionButton fabPlay, fabReset;
    private Spinner spinnerOptions;
    private Button button1, button2, button3, button4;
    private TextView receivedDataText;
    private CardView dataCardView;
    private SerialService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_monotonia);

        // Inicializar vistas
        fabPlay = findViewById(R.id.fabPlay);
        fabReset = findViewById(R.id.fabReset);
        spinnerOptions = findViewById(R.id.spinnerOptions);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        // Asumimos que tienes un TextView dentro de un CardView para mostrar datos recibidos
        receivedDataText = findViewById(R.id.receivedDataText);
        dataCardView = findViewById(R.id.cardView);

        // Vincular al servicio existente
        bindService(new Intent(this, SerialService.class), this, 0);

        // Configurar Spinner con opciones correctas
        String[] opciones = {"Selecciona una opción", "Aleatoriamente", "Horario", "Antihorario"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opciones);
        spinnerOptions.setAdapter(adapter);

        // Deshabilitar botones y Spinner al inicio
        deshabilitarBotonesYSpinner();

        // Botón Play -> Habilita el Spinner y muestra Reset
        fabPlay.setOnClickListener(v -> {
            fabReset.setVisibility(View.VISIBLE);
            spinnerOptions.setEnabled(true);
            spinnerOptions.setAlpha(1.0f); // Hacer que se vea normal
        });

        // Evento de selección del Spinner
        spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // Si no es la opción por defecto
                    habilitarBotones();

                    // Enviar comando según la opción seleccionada
                    String command = "";
                    switch (position) {
                        case 1: // Aleatoriamente
                            command = "M1";
                            break;
                        case 2: // Horario
                            command = "M2";
                            break;
                        case 3: // Antihorario
                            command = "M3";
                            break;
                    }

                    sendCommand(command);
                    Toast.makeText(test_Monotonia.this, "Enviado: " + command, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Asignar OnClickListener a los botones
        button1.setOnClickListener(v -> {
            sendCommand("rojo");
            Toast.makeText(test_Monotonia.this, "Enviado: rojo", Toast.LENGTH_SHORT).show();
            deshabilitarBotonesYSpinner();
        });

        button2.setOnClickListener(v -> {
            sendCommand("amarillo");
            Toast.makeText(test_Monotonia.this, "Enviado: amarillo", Toast.LENGTH_SHORT).show();
            deshabilitarBotonesYSpinner();
        });

        button3.setOnClickListener(v -> {
            sendCommand("azul");
            Toast.makeText(test_Monotonia.this, "Enviado: azul", Toast.LENGTH_SHORT).show();
            deshabilitarBotonesYSpinner();
        });

        button4.setOnClickListener(v -> {
            sendCommand("verde");
            Toast.makeText(test_Monotonia.this, "Enviado: verde", Toast.LENGTH_SHORT).show();
            deshabilitarBotonesYSpinner();
        });

        // Botón Reset -> Reinicia todo
        fabReset.setOnClickListener(v -> {
            // Deshabilita los botones y el Spinner
            deshabilitarBotonesYSpinner();

            // Oculta el botón Reset nuevamente
            fabReset.setVisibility(View.GONE);

            // Limpiar los datos acumulados en la variable global
            fullReceivedData.setLength(0);

            // Limpiar el texto en el TextView (pero sin ocultar la CardView)
            receivedDataText.setText("Esperando datos..."); // Mensaje en lugar de ocultarlo

            // Mantener la CardView visible
            dataCardView.setVisibility(View.VISIBLE);

            // Enviar el comando "S" por Bluetooth para indicar el reinicio
            sendCommand("S");

            // Mensaje de confirmación para el usuario
            Toast.makeText(test_Monotonia.this, "Datos reiniciados y enviado: S", Toast.LENGTH_SHORT).show();
        });


    }

    // Método para enviar comandos por Bluetooth
    private void sendCommand(String command) {
        if (service != null) {
            try {
                service.write(command.getBytes());
            } catch (IOException e) {
                Toast.makeText(this, "Error al enviar comando: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No conectado al dispositivo", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para habilitar los botones (quita la opacidad)
    private void habilitarBotones() {
        button1.setEnabled(true);
        button2.setEnabled(true);
        button3.setEnabled(true);
        button4.setEnabled(true);

        button1.setAlpha(1.0f);
        button2.setAlpha(1.0f);
        button3.setAlpha(1.0f);
        button4.setAlpha(1.0f);
    }

    // Método para deshabilitar botones y el Spinner (los hace opacos)
    private void deshabilitarBotonesYSpinner() {
        spinnerOptions.setEnabled(false);
        spinnerOptions.setAlpha(0.5f); // Opacar el Spinner
        spinnerOptions.setSelection(0); // Resetear selección

        button1.setEnabled(false);
        button2.setEnabled(false);
        button3.setEnabled(false);
        button4.setEnabled(false);

        button1.setAlpha(0.5f);
        button2.setAlpha(0.5f);
        button3.setAlpha(0.5f);
        button4.setAlpha(0.5f);
    }

    // Implementación de métodos de SerialListener
    @Override
    public void onSerialConnect() {
        runOnUiThread(() -> Toast.makeText(this, "Conectado y listo para usar", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onSerialConnectError(Exception e) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish(); // Volver a HomeTest si hay error
        });
    }

    @Override
    public void onSerialRead(byte[] data) {
        // Este método no se usa directamente, los datos llegan desde onSerialRead(ArrayDeque<byte[]>)
    }

    // Agrega esta variable global en tu clase
    private StringBuilder fullReceivedData = new StringBuilder();

    @Override
    public void onSerialRead(ArrayDeque<byte[]> datas) {
        runOnUiThread(() -> {
            for (byte[] data : datas) {
                fullReceivedData.append(new String(data)); // Acumular los datos
            }

            // Mostrar los datos completos en el TextView
            if (receivedDataText != null) {
                receivedDataText.setText("Datos recibidos: " + fullReceivedData.toString());
                dataCardView.setVisibility(View.VISIBLE);
            }
        });
    }



    @Override
    public void onSerialIoError(Exception e) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error de comunicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish(); // Volver a HomeTest si hay error
        });
    }

    // Implementación de métodos de ServiceConnection
    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    @Override
    public void onBackPressed() {
        // Solo volver a HomeTest, la desconexión se maneja allí
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (service != null) {
            service.detach();
            unbindService(this);
        }
        super.onDestroy();
    }
}