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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault()); // 🔥 Corrige el formato
        return sdf.format(new Date(timestamp));
    }

    /**
     * Obtiene datos de la colección "testResultados" donde el correo y la fecha coincidan.
     */
    public List<Map<String, Object>> getAllDataByEmailAndDate(String email, String selectedDate) {
        List<Map<String, Object>> allData = new ArrayList<>();

        try {
            CollectionReference collectionRef = db.collection("testResultados");

            // Obtener documentos con el correo del usuario
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

                        if (fechaDocumento.equals(selectedDate)) {
                            allData.add(data);
                        }
                    }
                }
            }

            Log.d("FirestoreService", "Datos filtrados por fecha: " + allData.size());
        } catch (ExecutionException | InterruptedException e) {
            Log.e("FirestoreService", "Error al obtener datos de Firestore", e);
        }

        return allData;
    }
}
