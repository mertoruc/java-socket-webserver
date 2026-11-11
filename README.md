# java-socket-webserver
# 🌐 Java Socket Tabanlı Çok İş Parçacıklı (Multi-Threaded) HTTP Web Sunucusu

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Protocol](https://img.shields.io/badge/Protocol-HTTP%2F1.1%20%7C%20TCP-blue?style=for-the-badge)
![Port](https://img.shields.io/badge/Port-1989-success?style=for-the-badge)
![Architecture](https://img.shields.io/badge/Architecture-SOLID%20Principles-orange?style=for-the-badge)

## 📖 1. Proje Özeti ve Amaç
Bu proje, herhangi bir 3. parti kütüphane (Spring Boot, Apache Tomcat, Netty, Jetty vb.) veya harici bir bileşen kullanılmadan, tamamen **Java Standart Kütüphanesi (`java.net` ve `java.io`)** kullanılarak sıfırdan geliştirilmiş bir HTTP Web Sunucusu (Web Server) uygulamasıdır.

Uygulamanın temel amacı; düşük seviyeli ağ programlama (Low-Level Network Programming) prensiplerini, **TCP/IP** soket mimarisini, **HTTP/1.1** uygulama katmanı protokolünü ve **SOLID** nesne yönelimli tasarım prensiplerini gerçek bir senaryo üzerinde uygulamaktır. Sunucu, yerel ağ üzerinde **1989** numaralı portu dinlemekte ve gelen istemci (web tarayıcısı) isteklerine dinamik olarak biçimlendirilmiş HTML5/CSS3 içeriği ile yanıt vermektedir.

---

## 🛠️ 2. Sistem Mimarisi ve Çalışma Akışı

Sunucu, bloklanmayan (non-blocking) bir deneyim sunmak amacıyla **Thread-Per-Request (İstek Başına İş Parçacığı)** mimarisi üzerine inşa edilmiştir. Bir web tarayıcısı `http://localhost:1989` adresine istek gönderdiğinde sistemde gerçekleşen yaşam döngüsü aşağıdaki gibidir:

```text
+--------------------+          1. TCP Bağlantı İsteği (SYN)          +-----------------------+
|                    | ---------------------------------------------> |                       |
|   İstemci (Web     |          2. Bağlantı Kabulü (SYN-ACK)          |     WebServer         |
|   Tarayıcısı)      | <--------------------------------------------- |  (ServerSocket:1989)  |
|  (Chrome, Safari)  |          3. HTTP GET / HTTP/1.1 İsteği         |                       |
|                    | ---------------------------------------------> +-----------+-----------+
+--------------------+                                                            |
          ^                                                                       | 4. accept() ile Socket üretilir
          |                                                                       |    ve yeni Thread başlatılır
          |                                                                       v
          |                                                           +-----------------------+
          |                                                           |     ClientHandler     |
          |                                                           |  (Runnable / Thread)  |
          |                                                           +-----------+-----------+
          |                                                                       |
          | 6. HTTP/1.1 200 OK Başlığı + HTML Byte Akışı                          | 5. getHtmlContent() çağrısı
          |    (OutputStream üzerinden gönderilir ve bağlantı kapatılır)          v
          |                                                           +-----------------------+
          +---------------------------------------------------------- |   ContentProvider     |
                                                                      | (StudentBiography...) |
                                                                      +-----------------------+
