package ucacue.edu.udipsai.Services;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import ucacue.edu.udipsai.Model.Patient;
import ucacue.edu.udipsai.R;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PacienteViewHolder> {
    private List<Patient> listaPacientes;

    public PatientAdapter(List<Patient> listaPacientes) {
        this.listaPacientes = listaPacientes;
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

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fecha = sdf.format(paciente.getFechaRegistro());
        holder.txtFecha.setText("Registrado: " + fecha);
    }

    @Override
    public int getItemCount() {
        return listaPacientes.size();
    }

    public static class PacienteViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtTelefono, txtFecha;

        public PacienteViewHolder(View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtTelefono = itemView.findViewById(R.id.txtTelefono);
            txtFecha = itemView.findViewById(R.id.txtFecha);
        }
    }
}
