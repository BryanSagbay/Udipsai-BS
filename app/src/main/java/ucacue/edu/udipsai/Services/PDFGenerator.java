package ucacue.edu.udipsai.Services;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class PDFGenerator {

    public static void generatePDF(OutputStream outputStream, String email, String date, List<Map<String, Object>> dataList) throws Exception {
        if (outputStream == null) {
            throw new Exception("OutputStream es nulo, no se puede generar el PDF.");
        }

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Agregar información del usuario y la fecha
        document.add(new Paragraph("📌 Reporte de Resultados").setBold().setFontSize(16));
        document.add(new Paragraph("Usuario: " + email).setFontSize(12));
        document.add(new Paragraph("Fecha: " + date).setFontSize(12));
        document.add(new Paragraph(" "));

        if (dataList.isEmpty()) {
            document.add(new Paragraph("No hay datos disponibles para esta fecha."));
        } else {
            for (Map<String, Object> data : dataList) {
                document.add(new Paragraph("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").setFontSize(10));

                // Mostrar tipo de test (Ejemplo: "Resultados del Test de Palanca")
                if (data.containsKey("testTipo")) {
                    document.add(new Paragraph("📌 " + data.get("testTipo").toString()).setBold().setFontSize(14));
                }

                document.add(new Paragraph(" "));

                Table table = new Table(2);
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // Excluir campos irrelevantes en el PDF
                    if (!key.equals("timestamp") && !key.equals("correoUsuario") && !key.equals("testTipo")) {
                        table.addCell(new Cell().add(new Paragraph(key).setBold()));
                        table.addCell(new Cell().add(new Paragraph(value.toString())));
                    }
                }

                document.add(table);
                document.add(new Paragraph(" "));
            }
        }

        document.close();
    }
}
