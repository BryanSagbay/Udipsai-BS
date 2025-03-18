package ucacue.edu.udipsai.UI.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import ucacue.edu.udipsai.Model.Patient;
import ucacue.edu.udipsai.R;
import ucacue.edu.udipsai.Services.PatientAdapter;
import ucacue.edu.udipsai.UI.home.HomePage;

public class PatientHome extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView txtNoDatos;
    private ImageButton backButton;
    private FloatingActionButton fabAdd;
    private FirebaseFirestore db;
    private List<Patient> listaPacientes;
    private PatientAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_patient);

        recyclerView = findViewById(R.id.recyclerViewPacientes);
        txtNoDatos = findViewById(R.id.txtNoDatos);
        fabAdd = findViewById(R.id.fab_add);
        backButton = findViewById(R.id.back_button);
        db = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaPacientes = new ArrayList<>();
        adapter = new PatientAdapter(listaPacientes);
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(PatientHome.this, HomePage.class);
            startActivity(homeIntent);
            finish();
        });

        cargarPacientes();

        fabAdd.setOnClickListener(v -> mostrarDialogoAgregarPaciente());
    }

    /**
     * Carga los pacientes desde Firestore y los ordena por fechaRegistro en orden descendente.
     */
    private void cargarPacientes() {
        db.collection("pacientes").orderBy("fechaRegistro", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaPacientes.clear();
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Patient paciente = doc.toObject(Patient.class);
                            if (paciente != null) {
                                listaPacientes.add(paciente);
                            }
                        }
                        txtNoDatos.setVisibility(View.GONE);
                    } else {
                        txtNoDatos.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Muestra un diálogo para agregar un nuevo paciente.
     */
    private void mostrarDialogoAgregarPaciente() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.modal_paciente, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText edtNombre = dialogView.findViewById(R.id.edtNombre);
        EditText edtApellido = dialogView.findViewById(R.id.edtApellido);
        RadioGroup radioGroupGenero = dialogView.findViewById(R.id.radioGroupGenero);
        EditText edtEdad = dialogView.findViewById(R.id.edtEdad);
        EditText edtDireccion = dialogView.findViewById(R.id.edtDireccion);
        EditText edtTelefono = dialogView.findViewById(R.id.edtTelefono);
        Button btnAgregar = dialogView.findViewById(R.id.btnAgregar);

        btnAgregar.setOnClickListener(v -> {
            String nombre = edtNombre.getText().toString().trim();
            String apellido = edtApellido.getText().toString().trim();
            String edadStr = edtEdad.getText().toString().trim();
            String direccion = edtDireccion.getText().toString().trim();
            String telefono = edtTelefono.getText().toString().trim();

            // Obtener el género seleccionado
            int selectedId = radioGroupGenero.getCheckedRadioButtonId();
            String genero = "";
            if (selectedId == R.id.rbMasculino) {
                genero = "Masculino";
            } else if (selectedId == R.id.rbFemenino) {
                genero = "Femenino";
            }

            if (nombre.isEmpty() || apellido.isEmpty() || genero.isEmpty() || edadStr.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            int edad = Integer.parseInt(edadStr);
            agregarPaciente(nombre, apellido, genero, edad, direccion, telefono);
            dialog.dismiss();
        });
    }

    /**
     * Agrega un nuevo paciente a Firestore con ID basado en timestamp.
     */
    private void agregarPaciente(String nombre, String apellido, String genero, int edad, String direccion, String telefono) {
        long timestamp = System.currentTimeMillis(); // Usar timestamp como ID

        Patient paciente = new Patient(nombre, apellido, genero, edad, direccion, telefono);

        db.collection("pacientes").document(String.valueOf(timestamp)) // Usar timestamp como ID
                .set(paciente)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Paciente agregado con éxito", Toast.LENGTH_SHORT).show();
                    cargarPacientes();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al agregar paciente", Toast.LENGTH_SHORT).show());
    }
}
