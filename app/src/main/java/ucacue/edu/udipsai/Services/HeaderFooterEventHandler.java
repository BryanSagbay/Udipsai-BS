package ucacue.edu.udipsai.Services;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import java.io.InputStream;

public class HeaderFooterEventHandler implements IEventHandler {

    private PdfDocument pdfDoc;
    private Image headerImage;
    private Image footerImage;

    public HeaderFooterEventHandler(PdfDocument pdfDoc) {
        this.pdfDoc = pdfDoc;
        try {
            // Cargar la plantilla
            InputStream templateStream = PDFGenerator.class.getClassLoader().getResourceAsStream("assets/Plantilla.pdf");
            if (templateStream != null) {
                PdfDocument templateDoc = new PdfDocument(new PdfReader(templateStream));

                // Extraer solo la primera página de la plantilla
                PdfPage templatePage = templateDoc.getPage(1);
                PdfFormXObject templateForm = templatePage.copyAsFormXObject(pdfDoc);

                float pageWidth = pdfDoc.getDefaultPageSize().getWidth();
                float pageHeight = pdfDoc.getDefaultPageSize().getHeight();

                // Definir encabezado (solo la parte superior)
                headerImage = new Image(templateForm).scaleToFit(pageWidth, 100);
                headerImage.setFixedPosition(0, pageHeight - 100); // Colocar en la parte superior

                // Definir pie de página (solo la parte inferior)
                footerImage = new Image(templateForm).scaleToFit(pageWidth, 80);
                footerImage.setFixedPosition(0, 0); // Pie de página en la parte inferior

                templateDoc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfPage page = docEvent.getPage();
        PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

        try {
            Document doc = new Document(pdfDoc);

            // Agregar encabezado
            if (headerImage != null) {
                doc.add(headerImage);
            }

            // Agregar pie de página
            if (footerImage != null) {
                doc.add(footerImage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        canvas.release();
    }
}
