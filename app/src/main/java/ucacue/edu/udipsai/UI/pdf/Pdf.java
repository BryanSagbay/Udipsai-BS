package ucacue.edu.udipsai.UI.pdf;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.OutputStream;
import java.util.*;

import ucacue.edu.udipsai.R;
import ucacue.edu.udipsai.Services.AuthService;
import ucacue.edu.udipsai.Services.FirestoreService;
import ucacue.edu.udipsai.Services.PDFGenerator;
import ucacue.edu.udipsai.UI.home.HomePage;
import ucacue.edu.udipsai.UI.test.HomeTest;
import ucacue.edu.udipsai.UI.test.test_Palanca;

public class Pdf extends AppCompatActivity {

    private TextView textViewCorreo, textViewFechaSeleccionada;
    private Button btnGenerarPDF;
    private ImageButton btnSeleccionarFecha;
    private ProgressBar progressBar;
    private DatePicker datePicker;
    private FirestoreService firestoreService;
    private String correoUsuario;
    private String fechaSeleccionada;
    private ImageButton backButton;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_home);

        // Vincular vistas
        textViewCorreo = findViewById(R.id.textViewCorreo);
        textViewFechaSeleccionada = findViewById(R.id.textViewFechaSeleccionada);
        btnGenerarPDF = findViewById(R.id.btnGenerarPDF);
        progressBar = findViewById(R.id.progressBar);
        datePicker = findViewById(R.id.datePicker);
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        backButton = findViewById(R.id.back_button);


        // Inicializar FirestoreService
        try {
            firestoreService = new FirestoreService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Obtener el correo del usuario autenticado
        correoUsuario = AuthService.getUserEmail();
        if (correoUsuario != null) {
            textViewCorreo.setText(correoUsuario);
        } else {
            textViewCorreo.setText("No autenticado");
            btnGenerarPDF.setEnabled(false);
        }

        // Ocultar el DatePicker por defecto
        datePicker.setVisibility(View.GONE);
        configurarDatePicker();

        // Eventos de clic
        btnSeleccionarFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGenerarPDF.setOnClickListener(v -> verificarPermisosYGenerarPDF());

        // Botón para regresar y desconectar Bluetooth
        backButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(Pdf.this, HomePage.class);
            startActivity(homeIntent);
            finish();
        });
    }

    private void mostrarDatePicker() {
        if (datePicker.getVisibility() == View.GONE) {
            datePicker.setVisibility(View.VISIBLE);
        } else {
            datePicker.setVisibility(View.GONE);
        }
    }

    private void configurarDatePicker() {
        Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int día = calendario.get(Calendar.DAY_OF_MONTH);

        datePicker.init(año, mes, día, (view, year, monthOfYear, dayOfMonth) -> {
            fechaSeleccionada = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
            textViewFechaSeleccionada.setText(fechaSeleccionada);
            Log.d("DEBUG", "Fecha seleccionada: " + fechaSeleccionada);
            datePicker.setVisibility(View.GONE);
        });

        // Deshabilitar fechas futuras
        datePicker.setMaxDate(calendario.getTimeInMillis());
    }

    private void verificarPermisosYGenerarPDF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                generarPDF();
            }
        } else {
            generarPDF();
        }
    }

    private void generarPDF() {
        if (correoUsuario.equals("No autenticado") || fechaSeleccionada == null || fechaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Seleccione una fecha válida", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGenerarPDF.setEnabled(false);

        new Thread(() -> {
            try {
                List<Map<String, Object>> resultados = firestoreService.getAllDataByEmailAndDate(correoUsuario, fechaSeleccionada);
                Log.d("PdfActivity", "Resultados encontrados: " + resultados.size());

                if (!resultados.isEmpty()) {
                    Uri pdfUri = guardarPDF(correoUsuario, fechaSeleccionada, resultados);
                    runOnUiThread(() -> {
                        if (pdfUri != null) {
                            Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al guardar el PDF", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "No hay datos para esta fecha", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnGenerarPDF.setEnabled(true);
                });
            }
        }).start();
    }

    private Uri guardarPDF(String email, String date, List<Map<String, Object>> dataList) {
        String fileName = "Reporte_" + email + "_" + date + ".pdf";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);

        if (uri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                PDFGenerator.generatePDF(outputStream, email, date, dataList);
                return uri;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generarPDF();
            } else {
                Toast.makeText(this, "Se requieren permisos para guardar el PDF", Toast.LENGTH_LONG).show();
            }
        }
    }
}
