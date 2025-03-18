package ucacue.edu.udipsai.UI.pdf;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.OutputStream;
import java.util.*;

import ucacue.edu.udipsai.R;
import ucacue.edu.udipsai.Services.AuthService;
import ucacue.edu.udipsai.Services.FirestoreService;
import ucacue.edu.udipsai.Services.PDFGenerator;
import ucacue.edu.udipsai.UI.home.HomePage;

public class Pdf extends AppCompatActivity {

    private TextView textViewCorreo, textViewFechaSeleccionada, errorMessage;
    private Button btnGenerarPDF;
    private ImageButton btnSeleccionarFecha, backButton;
    private DatePicker datePicker;
    private FirestoreService firestoreService;
    private String correoUsuario;
    private String fechaSeleccionada;
    private FrameLayout loadingOverlay, errorOverlay;
    private ImageView loadingGif, errorloadingGif;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_home);

        // Vincular vistas
        textViewCorreo = findViewById(R.id.textViewCorreo);
        textViewFechaSeleccionada = findViewById(R.id.textViewFechaSeleccionada);
        btnGenerarPDF = findViewById(R.id.btnGenerarPDF);
        datePicker = findViewById(R.id.datePicker);
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        backButton = findViewById(R.id.back_button);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        errorOverlay = findViewById(R.id.errorOverlay);
        errorMessage = findViewById(R.id.errorMessage);
        loadingGif = findViewById(R.id.loadingGif);
        errorloadingGif = findViewById(R.id.errorloadingGif);

        // Cargar los GIFs con Glide
        Glide.with(this).asGif().load(R.drawable.ic_carpeta).into(loadingGif);
        Glide.with(this).asGif().load(R.drawable.ic_error_login).into(errorloadingGif);

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

        // Ocultar DatePicker por defecto
        datePicker.setVisibility(View.GONE);
        configurarDatePicker();

        // Eventos de clic
        btnSeleccionarFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGenerarPDF.setOnClickListener(v -> verificarPermisosYGenerarPDF());

        // Botón para regresar
        backButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(Pdf.this, HomePage.class);
            startActivity(homeIntent);
            finish();
        });

        // Ocultar overlays por defecto
        loadingOverlay.setVisibility(View.GONE);
        errorOverlay.setVisibility(View.GONE);
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
            mostrarError("Seleccione una fecha válida");
            return;
        }

        mostrarLoading(true);
        btnGenerarPDF.setEnabled(false);

        new Thread(() -> {
            try {
                List<Map<String, Object>> resultados = firestoreService.getAllDataByEmailAndDate(correoUsuario, fechaSeleccionada);

                if (!resultados.isEmpty()) {
                    Uri pdfUri = guardarPDF(correoUsuario, fechaSeleccionada, resultados);
                    runOnUiThread(() -> {
                        if (pdfUri != null) {
                            Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_SHORT).show();
                        } else {
                            mostrarError("Error al guardar el PDF");
                        }
                    });
                } else {
                    mostrarError("No hay datos para esta fecha");
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarError("Error generando PDF");
            } finally {
                runOnUiThread(() -> {
                    mostrarLoading(false);
                    btnGenerarPDF.setEnabled(true);
                });
            }
        }).start();
    }

    private void mostrarLoading(boolean mostrar) {
        runOnUiThread(() -> {
            if (mostrar) {
                loadingOverlay.setVisibility(View.VISIBLE);
                loadingGif.setVisibility(View.VISIBLE);
            } else {
                loadingOverlay.setVisibility(View.GONE);
                loadingGif.setVisibility(View.GONE);
            }
        });
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> {
            errorMessage.setText(mensaje);
            errorOverlay.setVisibility(View.VISIBLE);
            errorloadingGif.setVisibility(View.VISIBLE);

            // Ocultar el error después de 3 segundos
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                errorOverlay.setVisibility(View.GONE);
                errorloadingGif.setVisibility(View.GONE);
                }, 3000);
        });
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
                mostrarError("Se requieren permisos para guardar el PDF");
            }
        }
    }
}
