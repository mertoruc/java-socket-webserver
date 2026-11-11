import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// Dependency Inversion & Open/Closed Principle: İçerik üretimi soyutlanmıştır.
interface ContentProvider {
    String getHtmlContent();
}

// Single Responsibility Principle: Sadece öğrenci biyografi HTML'ini üretir.
class StudentBiographyProvider implements ContentProvider {
    @Override
    public String getHtmlContent() {
        return "<!DOCTYPE html>" +
               "<html lang='tr'>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<title>Muhammet Mert Oruç - Biyografi</title>" +
               "<style>" +
               "body { background-color: #f4f7f6; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; margin: 40px; line-height: 1.6; }" +
               ".card { background: #ffffff; padding: 30px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); max-width: 700px; margin: auto; }" +
               "h1 { color: #1e3799; margin-bottom: 5px; font-family: 'Georgia', serif; }" +
               "h2 { color: #4a69bd; margin-top: 0; font-weight: 500; border-bottom: 2px solid #e0e0e0; padding-bottom: 10px; }" +
               "p { font-size: 16px; color: #2f3640; }" +
               ".highlight { color: #eb2f06; font-weight: bold; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='card'>" +
               "<h1>Muhammet Mert Oruç</h1>" +
               "<h2>1250505077</h2>" +
               "<p>Merhaba! Ben <span class='highlight'>Kırklareli Üniversitesi</span> Yazılım Mühendisliği bölümü öğrencisiyim. " +
               "Yazılım geliştirme süreçlerine, sistem mimarilerine ve algoritmalara büyük ilgi duyuyorum.</p>" +
               "<p>Özellikle <strong>C programlama, Java, Veri Yapıları ve Algoritmalar</strong> ile Web Teknolojileri üzerine projeler geliştiriyorum. " +
               "Üniversitemizin Teknofest kulübünde aktif rol alarak otonom sistemler ve takım çalışmaları yürütmekteyim.</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
}

// Single Responsibility Principle: Sadece gelen istemci bağlantısına HTTP yanıtı döner.
class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ContentProvider contentProvider;

    public ClientHandler(Socket clientSocket, ContentProvider contentProvider) {
        this.clientSocket = clientSocket;
        this.contentProvider = contentProvider;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null) return;
            System.out.println("Gelen İstek: " + requestLine);

            String html = contentProvider.getHtmlContent();
            byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);

            String httpHeader = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/html; charset=UTF-8\r\n" +
                                "Content-Length: " + htmlBytes.length + "\r\n" +
                                "Connection: close\r\n\r\n";

            out.write(httpHeader.getBytes(StandardCharsets.UTF_8));
            out.write(htmlBytes);
            out.flush();

        } catch (IOException e) {
            System.err.println("İstemci hatası: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

public class WebServer {
    private static final int PORT = 1989;

    public static void main(String[] args) {
        ContentProvider provider = new StudentBiographyProvider();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Sunucu başlatıldı. Port dinleniyor: " + PORT);
            System.out.println("Tarayıcıdan http://localhost:" + PORT + " adresine gidebilirsiniz.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(clientSocket, provider));
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Sunucu başlatılamadı: " + e.getMessage());
        }
    }
}
