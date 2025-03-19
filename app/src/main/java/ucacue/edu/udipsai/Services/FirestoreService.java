package ucacue.edu.udipsai.Services;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class FirestoreService {
    private final FirebaseFirestore db;

    public FirestoreService() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Convierte un timestamp (milisegundos) a una fecha en formato yyyy-M-d
     * para que coincida con la salida del DatePicker.
     */
    private String convertirTimestampAFecha(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Obtiene datos de la colección "testResultados" donde el correo, la fecha y el paciente coincidan.
     */
    public List<Map<String, Object>> getAllDataByEmailDateAndPaciente(
            String email,
            String selectedDate,
            String pacienteNombre
    ) {
        List<Map<String, Object>> allData = new ArrayList<>();

        try {
            CollectionReference collectionRef = db.collection("testResultados");

            // Construir la consulta base
            Task<QuerySnapshot> queryTask = collectionRef
                    .whereEqualTo("correoUsuario", email)
                    .get();

            QuerySnapshot querySnapshot = Tasks.await(queryTask);
            Log.d("FirestoreService", "Documentos recuperados: " + querySnapshot.getDocuments().size());

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    if (data != null && data.containsKey("timestamp")) {
                        long timestamp = (long) data.get("timestamp");
                        String fechaDocumento = convertirTimestampAFecha(timestamp);

                        Log.d("FirestoreService", "Fecha seleccionada: " + selectedDate + " - Fecha del documento: " + fechaDocumento);

                        // Filtrar por fecha y paciente
                        if (fechaDocumento.equals(selectedDate)) {
                            if (pacienteNombre.isEmpty() ||
                                    (data.containsKey("nombrePaciente") &&
                                            data.get("nombrePaciente").toString().equals(pacienteNombre))) {
                                allData.add(data);
                            }
                        }
                    }
                }
            }

            Log.d("FirestoreService", "Datos filtrados por fecha y paciente: " + allData.size());
        } catch (ExecutionException | InterruptedException e) {
            Log.e("FirestoreService", "Error al obtener datos de Firestore", e);
        }

        return allData;
    }

    /**
     * Obtiene la lista de pacientes asignados a un usuario específico.
     */
    public void getPacientesPorUsuario(String usuarioEmail, PacientesCallback callback) {
        db.collection("pacientes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> pacientes = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String nombre = doc.getString("nombre");
                        String apellido = doc.getString("apellido");
                        if (nombre != null && apellido != null) {
                            pacientes.add(nombre + " " + apellido); // Combina nombre y apellido
                        }
                    }
                    callback.onCallback(pacientes);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreService", "Error al obtener pacientes", e);
                    callback.onCallback(Collections.emptyList());
                });
    }

    /**
     * Interfaz para manejar la respuesta asíncrona de la lista de pacientes.
     */
    public interface PacientesCallback {
        void onCallback(List<String> pacientes);
    }
}