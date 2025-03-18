package ucacue.edu.udipsai.Services;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import ucacue.edu.udipsai.Model.Patient;
import ucacue.edu.udipsai.R;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PacienteViewHolder> {
    private List<Patient> listaPacientes;
    private Context context;
    private FirebaseFirestore db;

    public PatientAdapter(List<Patient> listaPacientes, Context context) {
        this.listaPacientes = listaPacientes;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
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
            int edad = Integer.parseInt(edtEdad.getText().toString().trim());

            if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar datos en Firestore
            db.collection("pacientes").document(String.valueOf(paciente.getId()))
                    .update("nombre", nombre, "apellido", apellido, "telefono", telefono, "direccion", direccion, "edad", edad)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Paciente actualizado", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show());
        });
    }

    private void eliminarPaciente(Patient paciente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminar paciente")
                .setMessage("¿Está seguro de que desea eliminar este paciente?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    db.collection("pacientes").document(String.valueOf(paciente.getId()))
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Paciente eliminado", Toast.LENGTH_SHORT).show();
                                listaPacientes.remove(paciente);
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public int getItemCount() {
        return listaPacientes.size();
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
