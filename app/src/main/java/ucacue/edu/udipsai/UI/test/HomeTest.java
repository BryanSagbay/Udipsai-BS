package ucacue.edu.udipsai.UI.test;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import ucacue.edu.udipsai.R;

public class HomeTest extends AppCompatActivity {
    private ImageView eagleImage;
    private TextView patientName;
    private Handler animationHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_test);

        // Referencias a los elementos
        eagleImage = findViewById(R.id.eagleImage);
        patientName = findViewById(R.id.patientName);
        LinearLayout btnMonotonia = findViewById(R.id.btnMonotonia);
        LinearLayout btnRiel = findViewById(R.id.btnRiel);
        LinearLayout btnPalanca = findViewById(R.id.btnPalanca);
        LinearLayout btnBennett = findViewById(R.id.btnBennett);
        ImageView logoutIcon = findViewById(R.id.logoutIcon);
        ImageView backIcon = findViewById(R.id.backIcon); // Icono de regreso

        // Simulación de asignación del paciente (En caso real, pasar desde Intent)
        patientName.setText("Juan Pérez");

        // Iniciar la animación del águila flotando
        startEagleFloatingAnimation();

        // Configurar eventos de clic para redirigir a las actividades correspondientes
        btnMonotonia.setOnClickListener(v -> openTestActivity(test_Monotonia.class));
        btnRiel.setOnClickListener(v -> openTestActivity(test_Riel.class));
        btnPalanca.setOnClickListener(v -> openTestActivity(test_Palanca.class));
        btnBennett.setOnClickListener(v -> openTestActivity(test_Bennett.class));

        // Evento de clic para regresar a la página anterior
        backIcon.setOnClickListener(v -> onBackPressed());
    }

    private void startEagleFloatingAnimation() {
        ObjectAnimator floatAnimation = ObjectAnimator.ofFloat(eagleImage, "translationY", -20f, 20f);
        floatAnimation.setDuration(2000);
        floatAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnimation.setRepeatMode(ValueAnimator.REVERSE);
        floatAnimation.setRepeatCount(ValueAnimator.INFINITE);
        floatAnimation.start();
    }

    private void openTestActivity(Class<?> testActivity) {
        Intent intent = new Intent(HomeTest.this, testActivity);
        startActivity(intent);
    }
}
