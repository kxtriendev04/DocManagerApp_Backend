package com.vn.document.service;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFont;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class WaterMarkService {

    @Value("${upload-file.base-uri}")
    private String uploadDirPath;

    // Thêm Watermark cho PDF
    public MultipartFile addWatermarkToPDF(MultipartFile file, String watermarkText) throws Exception {
        // Đọc tệp vào byte array
        byte[] fileBytes = file.getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileBytes);

        // Sử dụng iText để thêm watermark vào PDF
        PdfReader reader = new PdfReader(byteArrayInputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(reader, writer);

        int totalPages = pdfDocument.getNumberOfPages();
        PdfFont font = PdfFontFactory.createFont();

        // Thêm watermark vào các trang PDF
        for (int i = 1; i <= totalPages; i++) {
            PdfPage page = pdfDocument.getPage(i);
            PdfCanvas canvas = new PdfCanvas(page);
            canvas.beginText()
                    .setFontAndSize(font, 50)
                    .moveText(30, 30)
                    .showText(watermarkText)
                    .endText();
        }

        pdfDocument.close();

        // Đọc file đã watermark vào byte array từ ByteArrayOutputStream
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Tạo MultipartFile từ file đã watermark
        ByteArrayResource resource = new ByteArrayResource(byteArray);
        return new MultipartFile() {
            @Override
            public String getName() {
                return "watermarked-" + file.getOriginalFilename();
            }

            @Override
            public String getOriginalFilename() {
                return "watermarked-" + file.getOriginalFilename();
            }

            @Override
            public String getContentType() {
                return "application/pdf";
            }

            @Override
            public boolean isEmpty() {
                return byteArray.length == 0;
            }

            @Override
            public long getSize() {
                return byteArray.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return byteArray;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(byteArray);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.write(dest.toPath(), byteArray);
            }
        };
    }

    // Thêm Watermark cho DOCX
    public MultipartFile addWatermarkToDocx(MultipartFile file, String watermarkText) throws Exception {
        // Đọc tệp DOCX vào byte array
        byte[] fileBytes = file.getBytes();
        ByteArrayInputStream fis = new ByteArrayInputStream(fileBytes);

        // Mở tài liệu DOCX
        XWPFDocument document = new XWPFDocument(fis);

        // Thêm watermark vào tài liệu
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(watermarkText);
        run.setFontSize(60);
        run.setColor("B0B0B0");
        run.setTextPosition(100);

        // Ghi dữ liệu vào ByteArrayOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        document.write(byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        fis.close();

        // Tạo MultipartFile từ file đã watermark bằng ByteArrayResource
        ByteArrayResource resource = new ByteArrayResource(byteArray);
        return new MultipartFile() {
            @Override
            public String getName() {
                return "watermarked-" + file.getOriginalFilename();
            }

            @Override
            public String getOriginalFilename() {
                return "watermarked-" + file.getOriginalFilename();
            }

            @Override
            public String getContentType() {
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }

            @Override
            public boolean isEmpty() {
                return byteArray.length == 0;
            }

            @Override
            public long getSize() {
                return byteArray.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return byteArray;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(byteArray);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.write(dest.toPath(), byteArray);
            }
        };
    }

    // Thêm Watermark cho Hình Ảnh
    public MultipartFile addWatermarkToImage(MultipartFile file, String watermarkText) throws IOException {
        // Đọc hình ảnh vào byte array
        byte[] fileBytes = file.getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileBytes);

        // Mở file hình ảnh
        BufferedImage image = ImageIO.read(byteArrayInputStream);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        Font font = new Font("Arial", Font.BOLD, 60);
        g2d.setFont(font);
        g2d.setColor(new Color(255, 0, 0, 128)); // Màu đỏ với độ trong suốt
        g2d.drawString(watermarkText, 50, 50); // Vị trí của watermark

        // Ghi dữ liệu vào ByteArrayOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        g2d.dispose();

        // Tạo MultipartFile từ file đã watermark bằng ByteArrayResource
        ByteArrayResource resource = new ByteArrayResource(byteArray);
        return new MultipartFile() {
            @Override
            public String getName() {
                return "watermarked-" + file.getOriginalFilename();
            }

            @Override
            public String getOriginalFilename() {
                return "watermarked-" + file.getOriginalFilename();
            }

            @Override
            public String getContentType() {
                return "image/png";
            }

            @Override
            public boolean isEmpty() {
                return byteArray.length == 0;
            }

            @Override
            public long getSize() {
                return byteArray.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return byteArray;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(byteArray);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.write(dest.toPath(), byteArray);
            }
        };
    }

    // Thêm Watermark cho Video
    public MultipartFile addWatermarkToVideo(MultipartFile file, String watermarkText) throws IOException, InterruptedException {
        // Đọc video vào byte array
        byte[] fileBytes = file.getBytes();
        File tempFile = File.createTempFile("temp", ".mp4");
        Files.write(tempFile.toPath(), fileBytes);

        // Tạo output file cho video đã watermark
        File outputFile = new File(tempFile.getParent(), "watermarked-" + tempFile.getName());

        // Lệnh FFmpeg để thêm watermark vào video
        String command = "ffmpeg -i " + tempFile.getAbsolutePath() +
                " -vf drawtext=\"text=" + watermarkText + ":fontcolor=white@0.5:fontsize=30:x=10:y=10\" -codec:a copy " + outputFile.getAbsolutePath();

        // Thực thi lệnh FFmpeg
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();  // Chờ quá trình xử lý video hoàn tất

        // Đọc file đã watermark vào byte array
        byte[] byteArray = Files.readAllBytes(outputFile.toPath());

        // Xóa file tạm sau khi xử lý xong
        tempFile.delete();

        // Tạo MultipartFile từ file đã watermark bằng ByteArrayResource
        ByteArrayResource resource = new ByteArrayResource(byteArray);
        return new MultipartFile() {
            @Override
            public String getName() {
                return outputFile.getName();
            }

            @Override
            public String getOriginalFilename() {
                return outputFile.getName();
            }

            @Override
            public String getContentType() {
                return "video/mp4";
            }

            @Override
            public boolean isEmpty() {
                return byteArray.length == 0;
            }

            @Override
            public long getSize() {
                return byteArray.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return byteArray;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(byteArray);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.write(dest.toPath(), byteArray);
            }
        };
    }
}
