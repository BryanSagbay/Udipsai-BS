package ucacue.edu.udipsai.Services;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import ucacue.edu.udipsai.Model.Patient;
import ucacue.edu.udipsai.R;
import ucacue.edu.udipsai.UI.patient.PatientHome;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PacienteViewHolder> {
    private List<Patient> listaPacientes;
    private Context context;
    private FirebaseFirestore db;

    // Referencias a los overlays de la actividad principal
    private FrameLayout loadingOverlay;
    private FrameLayout errorOverlay;
    private TextView errorMessage;
    private ImageView loadingGif, errorloadingGif;


    public PatientAdapter(List<Patient> listaPacientes, Context context) {
        this.listaPacientes = listaPacientes;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();

        // Obtener referencias a los overlays si estamos en PatientHome
        if (context instanceof PatientHome) {
            PatientHome activity = (PatientHome) context;
            loadingOverlay = activity.findViewById(R.id.loadingOverlay);
            errorOverlay = activity.findViewById(R.id.errorOverlay);
            errorMessage = activity.findViewById(R.id.errorMessage);
            loadingGif = activity.findViewById(R.id.loadingGif);
            errorloadingGif = activity.findViewById(R.id.errorloadingGif);

            // Cargar los GIFs con Glide
            Glide.with(activity).asGif().load(R.drawable.ic_check).into(loadingGif);
            Glide.with(activity).asGif().load(R.drawable.ic_error_login).into(errorloadingGif);
        }
    }

    @NonNull
    @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listado_pacientes, parent, false);
        return new PacienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder holder, int position) {
        Patient paciente = listaPacientes.get(position);
        holder.txtNombre.setText(paciente.getNombre() + " " + paciente.getApellido());
        holder.txtTelefono.setText("Tel: " + paciente.getTelefono());

        // Evento para EDITAR
        holder.btnEditar.setOnClickListener(v -> mostrarDialogoEditar(paciente));

        // Evento para ELIMINAR
        holder.btnEliminar.setOnClickListener(v -> eliminarPaciente(paciente));
    }

    private void mostrarDialogoEditar(Patient paciente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.modal_paciente, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText edtNombre = dialogView.findViewById(R.id.edtNombre);
        EditText edtApellido = dialogView.findViewById(R.id.edtApellido);
        EditText edtTelefono = dialogView.findViewById(R.id.edtTelefono);
        EditText edtDireccion = dialogView.findViewById(R.id.edtDireccion);
        EditText edtEdad = dialogView.findViewById(R.id.edtEdad);
        Button btnActualizar = dialogView.findViewById(R.id.btnAgregar);

        // Rellenar los campos con datos existentes
        edtNombre.setText(paciente.getNombre());
        edtApellido.setText(paciente.getApellido());
        edtTelefono.setText(paciente.getTelefono());
        edtDireccion.setText(paciente.getDireccion());
        edtEdad.setText(String.valueOf(paciente.getEdad()));
        btnActualizar.setText("Actualizar");

        btnActualizar.setOnClickListener(v -> {
            String nombre = edtNombre.getText().toString().trim();
            String apellido = edtApellido.getText().toString().trim();
            String telefono = edtTelefono.getText().toString().trim();
            String direccion = edtDireccion.getText().toString().trim();
            String edadStr = edtEdad.getText().toString().trim();

            if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty() || direccion.isEmpty() || edadStr.isEmpty()) {
                showErrorAlert("Todos los campos son obligatorios");
                return;
            }

            int edad = Integer.parseInt(edadStr);
            dialog.dismiss();
            showLoadingIndicator();

            // Actualizar datos en Firestore
            db.collection("pacientes").document(String.valueOf(paciente.getId()))
                    .update("nombre", nombre, "apellido", apellido, "telefono", telefono, "direccion", direccion, "edad", edad)
                    .addOnSuccessListener(aVoid -> {
                        showSuccessAlert();
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> showErrorAlert("Error al actualizar: " + e.getMessage()));
        });
    }

    private void eliminarPaciente(Patient paciente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminar paciente")
                .setMessage("¿Está seguro de que desea eliminar este paciente?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    showLoadingIndicator();

                    db.collection("pacientes").document(String.valueOf(paciente.getId()))
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                showSuccessAlert();
                                listaPacientes.remove(paciente);
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> showErrorAlert("Error al eliminar: " + e.getMessage()));
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public int getItemCount() {
        return listaPacientes.size();
    }

    /**
     * Muestra el indicador de carga
     */
    private void showLoadingIndicator() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingGif.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Muestra la alerta de éxito
     */
    private void showSuccessAlert() {
        if (loadingOverlay != null) {
            // Mostrar el overlay de éxito
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingGif.setVisibility(View.VISIBLE);
            // Auto-ocultar después de 2 segundos
            new Handler().postDelayed(this::hideSuccessAlert, 2000);
        }
    }

    /**
     * Oculta la alerta de éxito
     */
    private void hideSuccessAlert() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
            loadingGif.setVisibility(View.GONE);
        }
    }

    /**
     * Muestra la alerta de error
     */
    private void showErrorAlert(String message) {
        if (errorOverlay != null && errorMessage != null) {
            errorMessage.setText(message);
            errorOverlay.setVisibility(View.VISIBLE);
            errorloadingGif.setVisibility(View.VISIBLE);

            // Auto-ocultar después de 3 segundos
            new Handler().postDelayed(this::hideErrorAlert, 3000);
        }
    }

    /**
     * Oculta la alerta de error
     */
    private void hideErrorAlert() {
        if (errorOverlay != null) {
            errorOverlay.setVisibility(View.GONE);
            errorloadingGif.setVisibility(View.GONE);
        }
    }

    public static class PacienteViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtTelefono;
        ImageButton btnEditar, btnEliminar;

        public PacienteViewHolder(View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtTelefono = itemView.findViewById(R.id.txtTelefono);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}