package tn.esprit.projetintegre.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.Sponsor;
import tn.esprit.projetintegre.entities.Sponsorship;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SponsorshipEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Generate PDF sponsorship receipt
     */
    public byte[] generateSponsorshipReceipt(Sponsorship sponsorship) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            Document document = new Document(pdfDoc);

            // Header
            document.add(new Paragraph("CAMP CONNECT")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(ColorConstants.GREEN)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Sponsorship Agreement")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Receipt ID and Date
            Table headerTable = new Table(new float[]{1, 1});
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.addCell(createCell("Receipt ID:", true));
            headerTable.addCell(createCell("SP-" + sponsorship.getId(), false));
            headerTable.addCell(createCell("Date:", true));
            headerTable.addCell(createCell(
                    sponsorship.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), false));
            document.add(headerTable);

            document.add(new Paragraph("\n"));

            // Sponsor Information
            document.add(new Paragraph("SPONSOR INFORMATION")
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            Sponsor sponsor = sponsorship.getSponsor();
            Table sponsorTable = new Table(new float[]{1, 2});
            sponsorTable.setWidth(UnitValue.createPercentValue(100));
            sponsorTable.addCell(createCell("Company Name:", true));
            sponsorTable.addCell(createCell(sponsor.getName(), false));
            sponsorTable.addCell(createCell("Contact Email:", true));
            sponsorTable.addCell(createCell(sponsor.getEmail(), false));
            sponsorTable.addCell(createCell("Contact Person:", true));
            sponsorTable.addCell(createCell(sponsor.getContactPerson() != null ? sponsor.getContactPerson() : "N/A", false));
            sponsorTable.addCell(createCell("Phone:", true));
            sponsorTable.addCell(createCell(sponsor.getPhone() != null ? sponsor.getPhone() : "N/A", false));
            document.add(sponsorTable);

            document.add(new Paragraph("\n"));

            // Event Information
            document.add(new Paragraph("EVENT INFORMATION")
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            Event event = sponsorship.getEvent();
            Table eventTable = new Table(new float[]{1, 2});
            eventTable.setWidth(UnitValue.createPercentValue(100));
            eventTable.addCell(createCell("Event Name:", true));
            eventTable.addCell(createCell(event.getTitle(), false));
            eventTable.addCell(createCell("Description:", true));
            eventTable.addCell(createCell(event.getDescription() != null ? event.getDescription() : "N/A", false));
            eventTable.addCell(createCell("Location:", true));
            eventTable.addCell(createCell(event.getLocation() != null ? event.getLocation() : "N/A", false));
            eventTable.addCell(createCell("Start Date:", true));
            eventTable.addCell(createCell(event.getStartDate() != null
                    ? event.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A", false));
            document.add(eventTable);

            document.add(new Paragraph("\n"));

            // Sponsorship Details
            document.add(new Paragraph("SPONSORSHIP DETAILS")
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            Table detailsTable = new Table(new float[]{1, 2});
            detailsTable.setWidth(UnitValue.createPercentValue(100));
            detailsTable.addCell(createCell("Sponsorship Type:", true));
            detailsTable.addCell(createCell(sponsorship.getSponsorshipType(), false));
            detailsTable.addCell(createCell("Sponsorship Level:", true));
            detailsTable.addCell(createCell(sponsorship.getSponsorshipLevel() != null 
                    ? sponsorship.getSponsorshipLevel() : "N/A", false));
            detailsTable.addCell(createCell("Amount:", true));
            detailsTable.addCell(createCell(sponsorship.getAmount() + " " + sponsorship.getCurrency(), false));
            detailsTable.addCell(createCell("Duration:", true));
            detailsTable.addCell(createCell(sponsorship.getStartDate().toString() + " - " + sponsorship.getEndDate().toString(), false));
            detailsTable.addCell(createCell("Status:", true));
            detailsTable.addCell(createCell(sponsorship.getStatus(), false));
            detailsTable.addCell(createCell("Benefits:", true));
            detailsTable.addCell(createCell(sponsorship.getBenefits() != null ? sponsorship.getBenefits() : "N/A", false));
            document.add(detailsTable);

            document.add(new Paragraph("\n"));

            // Footer
            document.add(new Paragraph("Thank you for sponsoring this event!")
                    .setFontSize(12)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("For any questions, please contact us at support@campconnect.com")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating sponsorship receipt PDF", e);
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }

    /**
     * Send sponsorship request email with PDF attachment
     */
    public void sendSponsorshipRequestEmail(Sponsorship sponsorship) {
        try {
            byte[] pdfBytes = generateSponsorshipReceipt(sponsorship);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Sponsor sponsor = sponsorship.getSponsor();
            Event event = sponsorship.getEvent();

            helper.setFrom(fromEmail);
            helper.setTo(sponsor.getEmail());
            helper.setSubject("Sponsorship Request: " + event.getTitle());

            // Build email content
            String emailContent = buildSponsorshipRequestEmail(sponsorship);
            helper.setText(emailContent, true);

            // Attach PDF
            ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
            helper.addAttachment("Sponsorship_Agreement_" + sponsorship.getId() + ".pdf", dataSource);

            mailSender.send(message);
            log.info("Sponsorship request email sent to {} for sponsorship {}", sponsor.getEmail(), sponsorship.getId());

        } catch (MessagingException e) {
            log.error("Error sending sponsorship request email", e);
            throw new RuntimeException("Failed to send sponsorship request email", e);
        }
    }

    /**
     * Send sponsorship acceptance confirmation email
     */
    public void sendSponsorshipAcceptedEmail(Sponsorship sponsorship) {
        try {
            byte[] pdfBytes = generateSponsorshipReceipt(sponsorship);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Sponsor sponsor = sponsorship.getSponsor();
            Event event = sponsorship.getEvent();

            helper.setFrom(fromEmail);
            helper.setTo(sponsor.getEmail());
            helper.setSubject("Sponsorship Accepted: " + event.getTitle());

            // Build email content
            String emailContent = buildSponsorshipAcceptedEmail(sponsorship);
            helper.setText(emailContent, true);

            // Attach PDF
            ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
            helper.addAttachment("Sponsorship_Confirmation_" + sponsorship.getId() + ".pdf", dataSource);

            mailSender.send(message);
            log.info("Sponsorship acceptance email sent to {} for sponsorship {}", sponsor.getEmail(), sponsorship.getId());

        } catch (MessagingException e) {
            log.error("Error sending sponsorship acceptance email", e);
            throw new RuntimeException("Failed to send sponsorship acceptance email", e);
        }
    }

    /**
     * Send sponsorship declined notification email
     */
    public void sendSponsorshipDeclinedEmail(Sponsorship sponsorship) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            Sponsor sponsor = sponsorship.getSponsor();
            Event event = sponsorship.getEvent();

            helper.setFrom(fromEmail);
            helper.setTo(sponsor.getEmail());
            helper.setSubject("Sponsorship Declined: " + event.getTitle());

            String emailContent = buildSponsorshipDeclinedEmail(sponsorship);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Sponsorship declined email sent to {} for sponsorship {}", sponsor.getEmail(), sponsorship.getId());

        } catch (MessagingException e) {
            log.error("Error sending sponsorship declined email", e);
            throw new RuntimeException("Failed to send sponsorship declined email", e);
        }
    }

    /**
     * Build sponsorship request email HTML content
     */
    private String buildSponsorshipRequestEmail(Sponsorship sponsorship) {
        Sponsor sponsor = sponsorship.getSponsor();
        Event event = sponsorship.getEvent();
        String acceptUrl = frontendUrl + "/sponsor/sponsorships/" + sponsorship.getId() + "/accept";
        String declineUrl = frontendUrl + "/sponsor/sponsorships/" + sponsorship.getId() + "/decline";

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #2d5a27;">Sponsorship Invitation</h2>
                <p>Dear %s,</p>
                <p>You have been invited to sponsor the following event:</p>
                <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #2d5a27; margin: 20px 0;">
                    <h3 style="margin-top: 0;">%s</h3>
                    <p><strong>Amount:</strong> %s %s</p>
                    <p><strong>Sponsorship Type:</strong> %s</p>
                    <p><strong>Duration:</strong> %s to %s</p>
                </div>
                <p>Please review the attached sponsorship agreement PDF for all details.</p>
                <p>To respond to this invitation, please log in to your sponsor dashboard:</p>
                <div style="margin: 20px 0;">
                    <a href="%s/sponsor/events" style="background-color: #2d5a27; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">View in Dashboard</a>
                </div>
                <p>If you have any questions, please contact us at support@campconnect.com</p>
                <p>Best regards,<br>The Camp Connect Team</p>
            </body>
            </html>
            """,
            sponsor.getName(),
            event.getTitle(),
            sponsorship.getAmount(),
            sponsorship.getCurrency(),
            sponsorship.getSponsorshipType(),
            sponsorship.getStartDate(),
            sponsorship.getEndDate(),
            frontendUrl
        );
    }

    /**
     * Build sponsorship accepted email HTML content
     */
    private String buildSponsorshipAcceptedEmail(Sponsorship sponsorship) {
        Sponsor sponsor = sponsorship.getSponsor();
        Event event = sponsorship.getEvent();

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #2d5a27;">Sponsorship Confirmed!</h2>
                <p>Dear %s,</p>
                <p>Thank you for accepting the sponsorship for:</p>
                <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #2d5a27; margin: 20px 0;">
                    <h3 style="margin-top: 0;">%s</h3>
                    <p><strong>Amount:</strong> %s %s</p>
                    <p><strong>Sponsorship Type:</strong> %s</p>
                    <p><strong>Status:</strong> ACCEPTED</p>
                </div>
                <p>Your sponsorship confirmation PDF is attached. Please keep this for your records.</p>
                <p>We look forward to working with you!</p>
                <p>Best regards,<br>The Camp Connect Team</p>
            </body>
            </html>
            """,
            sponsor.getName(),
            event.getTitle(),
            sponsorship.getAmount(),
            sponsorship.getCurrency(),
            sponsorship.getSponsorshipType()
        );
    }

    /**
     * Build sponsorship declined email HTML content
     */
    private String buildSponsorshipDeclinedEmail(Sponsorship sponsorship) {
        Sponsor sponsor = sponsorship.getSponsor();
        Event event = sponsorship.getEvent();

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #666;">Sponsorship Declined</h2>
                <p>Dear %s,</p>
                <p>You have declined the sponsorship opportunity for:</p>
                <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #666; margin: 20px 0;">
                    <h3 style="margin-top: 0;">%s</h3>
                    <p><strong>Amount:</strong> %s %s</p>
                    <p><strong>Status:</strong> DECLINED</p>
                </div>
                <p>If you change your mind or have any questions, please contact us at support@campconnect.com</p>
                <p>Best regards,<br>The Camp Connect Team</p>
            </body>
            </html>
            """,
            sponsor.getName(),
            event.getTitle(),
            sponsorship.getAmount(),
            sponsorship.getCurrency()
        );
    }

    private Cell createCell(String text, boolean isHeader) {
        Cell cell = new Cell();
        Paragraph paragraph = new Paragraph(text);
        if (isHeader) {
            paragraph.setBold();
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        }
        cell.add(paragraph);
        cell.setPadding(5);
        return cell;
    }
}
