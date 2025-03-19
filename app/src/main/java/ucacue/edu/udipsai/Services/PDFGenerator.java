package ucacue.edu.udipsai.Services;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class PDFGenerator {

    public static void generatePDF(
            OutputStream outputStream,
            String email,
            String date,
            List<Map<String, Object>> dataList,
            String pacienteNombre // Nuevo parámetro
    ) throws Exception {
        if (outputStream == null) {
            throw new Exception("OutputStream es nulo, no se puede generar el PDF.");
        }

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Cargar una fuente que soporte caracteres especiales
        PdfFont font = PdfFontFactory.createFont("assets/fonts/segoe-ui-emoji.ttf", PdfEncodings.IDENTITY_H);

        // Agregar información del usuario, fecha y paciente
        document.add(new Paragraph("📌 Reporte de Resultados").setFont(font).setBold().setFontSize(16));
        document.add(new Paragraph("Usuario: " + email).setFont(font).setFontSize(12));
        document.add(new Paragraph("Fecha: " + date).setFont(font).setFontSize(12));

        // Agregar el nombre del paciente si está presente
        if (pacienteNombre != null && !pacienteNombre.isEmpty()) {
            document.add(new Paragraph("Paciente: " + pacienteNombre).setFont(font).setFontSize(12));
        }

        document.add(new Paragraph(" ")); // Espacio en blanco

        if (dataList.isEmpty()) {
            document.add(new Paragraph("No hay datos disponibles para esta fecha.").setFont(font));
        } else {
            for (Map<String, Object> data : dataList) {
                // Mostrar tipo de test (Ejemplo: "Resultados del Test de Palanca")
                if (data.containsKey("titulo")) {
                    document.add(new Paragraph("📌 " + data.get("titulo").toString()).setFont(font).setBold().setFontSize(14));
                }

                document.add(new Paragraph(" ")); // Espacio en blanco

                // Crear una tabla para mostrar los datos
                Table table = new Table(2); // 2 columnas: clave y valor
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // Excluir campos irrelevantes en el PDF
                    if (!key.equals("timestamp") &&
                            !key.equals("correoUsuario") &&
                            !key.equals("titulo") &&
                            !key.equals("nombrePaciente")) {
                        table.addCell(new Cell().add(new Paragraph(key).setFont(font).setBold()));
                        table.addCell(new Cell().add(new Paragraph(value.toString()).setFont(font)));
                    }
                }

                document.add(table);
                document.add(new Paragraph(" ")); // Espacio en blanco entre tablas
            }
        }

        document.close();
    }
}