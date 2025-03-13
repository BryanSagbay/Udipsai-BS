package ucacue.edu.udipsai.UI.test;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;



import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ucacue.edu.udipsai.R;
import ucacue.edu.udipsai.Services.SerialListener;
import ucacue.edu.udipsai.Services.SerialService;
import ucacue.edu.udipsai.Services.SerialSocket;

public class HomeTest extends AppCompatActivity implements SerialListener, ServiceConnection {
    private TextView patientName;
    private SerialService service;
    private SerialSocket socket;
    private boolean isConnected = false;
    private String currentMacAddress = null;

    // Diccionario de botones y direcciones MAC
    private final Map<Integer, String> macAddresses = new HashMap<Integer, String>() {{
        put(R.id.btnMonotonia, "98:D3:71:FD:80:8B");
        put(R.id.btnRiel, "98:D3:31:F6:5D:9D");
        put(R.id.btnPalanca, "00:22:03:01:3C:45");
        put(R.id.btnBennett, "98:D3:11:FC:3B:3D");
    }};

    private final Map<Integer, Class<?>> testActivities = new HashMap<Integer, Class<?>>() {{
        put(R.id.btnMonotonia, test_Monotonia.class);
        put(R.id.btnRiel, test_Riel.class);
        put(R.id.btnPalanca, test_Palanca.class);
        put(R.id.btnBennett, test_Bennett.class);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_test);

        patientName = findViewById(R.id.patientName);
        patientName.setText("Juan Pérez");

        disconnect();

        startService(new Intent(this, SerialService.class));
        bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);

        // Vinculamos cada botón con su dirección MAC y actividad
        for (Map.Entry<Integer, String> entry : macAddresses.entrySet()) {
            findViewById(entry.getKey()).setOnClickListener(v -> connectToDevice(entry.getValue(), testActivities.get(entry.getKey())));
        }

        // Verificar si el Intent tiene el Extra "disconnected"
        if (getIntent() != null && getIntent().getBooleanExtra("disconnected", false)) {
            showDisconnected();
        }

        startEagleFloatingAnimation();
    }

    private void startEagleFloatingAnimation() {
        ObjectAnimator floatAnimation = ObjectAnimator.ofFloat(findViewById(R.id.eagleImage), "translationY", -20f, 20f);
        floatAnimation.setDuration(2000);
        floatAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnimation.setRepeatMode(ValueAnimator.REVERSE);
        floatAnimation.setRepeatCount(ValueAnimator.INFINITE);
        floatAnimation.start();
    }

    /**
     * Inicia la conexión con el dispositivo Bluetooth según su dirección MAC.
     */
    private static final int REQUEST_BLUETOOTH_CONNECT = 1;

    private void connectToDevice(String macAddress, Class<?> nextActivity) {
        // Verificar si el permiso está concedido (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar el permiso
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT);
            return;
        }

        // Continuar con la conexión si el permiso está concedido
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth no disponible o desactivado", Toast.LENGTH_LONG).show();
            return;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        if (device == null) {
            Toast.makeText(this, "Dispositivo no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            socket = new SerialSocket(getApplicationContext(), device);
            currentMacAddress = macAddress;
            isConnected = true;
            service.connect(socket);
            Toast.makeText(this, "Conectando a " + macAddress, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al conectar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Finaliza la conexión Bluetooth cuando el usuario regresa a HomeTest.
     */
    private void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket = null;
            isConnected = false;
            currentMacAddress = null;
        }
    }

    /**
     * Implementación de SerialListener para manejar eventos de conexión Bluetooth.
     */
    @Override
    public void onSerialConnect() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Conexión exitosa", Toast.LENGTH_SHORT).show();
            // Redirigir al usuario a la actividad correspondiente después de la conexión
            Intent intent = new Intent(this, testActivities.get(getCurrentButtonId()));
            startActivity(intent);
        });
    }

    @Override
    public void onSerialConnectError(Exception e) {
        runOnUiThread(() -> Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
    }

    @Override
    public void onSerialRead(java.util.ArrayDeque<byte[]> datas) {
    }

    @Override
    public void onSerialIoError(Exception e) {
        runOnUiThread(() -> {
            showDisconnectedDialog();
            disconnect();
            Intent intent = new Intent(this, HomeTest.class);
            startActivity(intent);
        });
    }

    /**
     * Lógica para asociar la conexión a su botón correspondiente
     */
    private int getCurrentButtonId() {
        for (Map.Entry<Integer, String> entry : macAddresses.entrySet()) {
            if (entry.getValue().equals(currentMacAddress)) {
                return entry.getKey();
            }
        }
        return -1;
    }

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
    protected void onDestroy() {
        disconnect();
        super.onDestroy();
    }
    /**
     * Alertas
     */

    private void showDisconnectedDialog() {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Conexión Perdida")
                    .setMessage("Se perdió la conexión con el dispositivo. Por favor, vuelva a conectarse.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    // Método para mostrar la alerta de desconexión
    private void showDisconnected() {
        View rootView = findViewById(android.R.id.content); // Obtiene la vista raíz
        Snackbar.make(rootView, "Conexión finalizada", Snackbar.LENGTH_LONG).show();
    }


}
