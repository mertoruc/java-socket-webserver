# 🌐 Java Socket Tabanlı Çok İş Parçacıklı (Multi-Threaded) HTTP Web Sunucusu

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Protocol](https://img.shields.io/badge/Protocol-HTTP%2F1.1%20%7C%20TCP%2FIP-blue?style=for-the-badge)
![Port](https://img.shields.io/badge/Port-1989-success?style=for-the-badge)
![Architecture](https://img.shields.io/badge/Architecture-SOLID%20Principles-purple?style=for-the-badge)

## 📖 1. Proje Özeti ve Amaç

Bu proje, herhangi bir 3. parti kütüphane (Spring Boot, Apache Tomcat, Netty, Jetty vb.) veya harici bir bileşen **kullanılmadan**, tamamen **Java Standart Kütüphanesi (`java.net` ve `java.io`)** kullanılarak sıfırdan geliştirilmiş çok iş parçacıklı (multi-threaded) bir **HTTP/1.1 Web Sunucusu** uygulamasıdır.

Uygulamanın temel amacı; düşük seviyeli ağ programlama (Low-Level Network Programming) prensiplerini, **TCP/IP** soket mimarisini, **HTTP/1.1** uygulama katmanı protokolünü ve **SOLID** nesne yönelimli tasarım prensiplerini gerçek bir senaryo üzerinde uygulamaktır. Sunucu, yerel ağ üzerinde **1989** numaralı portu dinlemekte ve gelen istemci (web tarayıcısı) isteklerine dinamik olarak biçimlendirilmiş (H1, H2 başlıkları ve özel CSS stilleri içeren) HTML5/CSS3 içeriği ile yanıt vermektedir.

---

## 🛠️ 2. Sistem Mimarisi ve Çalışma Akışı

Sunucu, bloklanmayan (non-blocking accept) bir deneyim sunmak amacıyla **Thread-Per-Request (İstek Başına İş Parçacığı)** mimarisi üzerine inşa edilmiştir. Bir web tarayıcısı `http://localhost:1989` adresine istek gönderdiğinde sistemde gerçekleşen yaşam döngüsü adım adım şu şekildedir:

1. **TCP Bağlantı Kurulumu (3-Way Handshake):** İstemci (Chrome, Safari vb.) `127.0.0.1:1989` adresine TCP SYN paketi gönderir ve `ServerSocket` bağlantıyı kabul eder.
2. **İsteğin Yakalanması (`accept()`):** `WebServer` sınıfı gelen bağlantıyı yakalar, o istemciye özel bir `Socket` nesnesi üretir ve ana döngüyü kilitlememek için yeni bir `Thread` (`ClientHandler`) başlatır.
3. **HTTP İsteğinin Okunması:** `ClientHandler`, `BufferedReader` aracılığıyla tarayıcının gönderdiği `GET / HTTP/1.1` istek satırını okur.
4. **İçeriğin Üretilmesi (Polimorfik Çağrı):** `ClientHandler`, `ContentProvider` arayüzü üzerinden `StudentBiographyProvider.getHtmlContent()` metodunu çağırarak HTML5/CSS3 metnini alır.
5. **HTTP Yanıtının Gönderilmesi ve Kapanış:** Üretilen HTML verisi `UTF-8` byte dizisine çevrilir, önüne `HTTP/1.1 200 OK` başlıkları eklenerek `OutputStream` üzerinden tarayıcıya yazılır ve soket güvenli şekilde kapatılır.

---

## ⚙️ 3. Uygulama Yazılırken Kullanılan Yaklaşımlar ve Çalışma Prensipleri

Uygulamanın geliştirilmesinde **Java I/O (Giriş/Çıkış Akışları)**, **Java Networking (`java.net`)**, **Otomatik Kaynak Yönetimi (`try-with-resources`)** ve **Multi-Threading (Çoklu İş Parçacığı)** yaklaşımları bir arada kullanılmıştır.

### 3.1. Kullanılan Nesneler, Özellikleri ve Görevleri

Projede yer alan her bir Java sınıfı ve nesnesi, ağ iletişiminin belirli bir katmanını yönetmek üzere seçilmiştir:

| Sınıf / Nesne | Bulunduğu Paket | Temel Özelliği ve Uygulamadaki Görevi |
| :--- | :--- | :--- |
| **`ServerSocket`** | `java.net.ServerSocket` | İşletim sisteminin ağ katmanından **1989** numaralı TCP portunu uygulamaya tahsis eder (*Binding*). `accept()` metodu aracılığıyla bu porta dışarıdan gelecek bağlantı taleplerini sürekli olarak dinler. Bir istemci bağlanana kadar ana iş parçacığını uyku modunda bekletir (blocking) ve gereksiz CPU tüketmez. |
| **`Socket`** | `java.net.Socket` | İstemci (tarayıcı) ile sunucu arasında kurulan sanal iletişim hattının (uç noktanın) nesne karşılığıdır. İstemcinin IP/Port bilgilerini ve çift yönlü veri okuma (`InputStream`) ile veri yazma (`OutputStream`) kanallarını barındırır. |
| **`InputStreamReader`** | `java.io.InputStreamReader` | Soketten fiziksel katmanda gelen ham byte akışını (`InputStream`), karakter akışına dönüştüren bir köprü (*Bridge/Adapter*) görevi görür. |
| **`BufferedReader`** | `java.io.BufferedReader` | Karakter akışını bellekte tamponlayarak (*buffering*) satır satır (`readLine()`) okuma imkanı sunar. Her karakter için işletim sistemine ayrı G/Ç (I/O) çağrısı yapmak yerine tarayıcının gönderdiği `GET / HTTP/1.1` başlığını yüksek performansla okur. |
| **`OutputStream`** | `java.io.OutputStream` | Sunucunun ürettiği HTTP yanıt başlıklarını ve HTML içeriğini byte dizileri (`byte[]`) halinde ağ üzerinden istemciye ileten çıkış akışıdır. `flush()` metodu ile tamponda bekleyen verinin anında ağ kartına gönderilmesi sağlanır. |
| **`StandardCharsets`** | `java.nio.charset` | Türkçe karakterlerin (`ğ, ü, ş, i, ö, ç, İ, Ğ`) farklı işletim sistemlerinde ve tarayıcılarda bozuk görünmemesi için metinlerin `UTF-8` standartlarında byte dizisine çevrilmesini garanti eder. |
| **`Thread` & `Runnable`** | `java.lang` | Sunucunun aynı anda birden fazla tarayıcı sekmesine yanıt verebilmesini (*Concurrency*) sağlar. Ana sunucu döngüsü sadece bağlantıyı kabul eder ve işlemi `ClientHandler` adlı yeni bir `Thread`'e devrederek hemen yeni bağlantıları dinlemeye geri döner. |

### 3.2. HTTP/1.1 Protokolü ve Paket Yapısı

3. parti bir kütüphane kullanılmadığı için, web tarayıcılarının anlayabileceği **HTTP (HyperText Transfer Protocol)** yanıt formatı soket üzerinden manuel olarak inşa edilmiştir:

* **Durum Satırı (`HTTP/1.1 200 OK\r\n`):** Tarayıcıya isteğin başarılı bir şekilde işlendiğini bildirir.
* **İçerik Tipi (`Content-Type: text/html; charset=UTF-8\r\n`):** Gönderilen byte akışının düz metin değil, render edilmesi gereken bir HTML belgesi olduğunu ve karakter kodlamasının UTF-8 olduğunu belirtir.
* **İçerik Boyutu (`Content-Length: ...\r\n`):** Gönderilen HTML gövdesinin byte cinsinden tam uzunluğunu bildirir; tarayıcı kaç byte okuduktan sonra sayfa yüklemesinin tamamlandığını bu başlıkla anlar.
* **Başlık-Gövde Ayırıcı (`\r\n\r\n`):** HTTP standartlarına göre üstbilgiler (Headers) ile asıl HTML içeriği (Body) arasındaki zorunlu boş satırdır.

---

## 🌍 4. Socket, Port ve IP Kavramları

Ağ programlamada iki farklı cihazın veya aynı cihazdaki iki farklı sürecin (process) birbiriyle hatasız veri alışverişi yapabilmesi; **IP Adresi**, **Port** ve **Socket** kavramlarının hiyerarşik uyumuna dayanır:

### 4.1. IP Adresi (Internet Protocol Address)
* **Tanım:** TCP/IP ağına bağlı olan her bir cihazın (bilgisayar, sunucu, telefon, yönlendirici) ağ üzerinde tanınmasını ve veri paketlerinin doğru hedefe yönlendirilmesini sağlayan benzersiz mantıksal kimlik numarasıdır. OSI referans modelinin **Ağ Katmanında (Network Layer - Layer 3)** çalışır.
* **Benzetme:** Gerçek hayattaki bir apartmanın açık posta adresidir; paketin hangi bilgisayara gideceğini belirler.
* **Uygulamadaki Rolü:** Yerel geliştirme ve test ortamında geri dönüş (*loopback*) adresi olan **`127.0.0.1`** (veya alan adı karşılığı olan **`localhost`**) kullanılmıştır. Bu adres, veri paketlerinin dış ağa çıkmadan doğrudan bilgisayarın kendi ağ yığınına yönlendirilmesini sağlar.

### 4.2. Port (Kapı Numarası)
* **Tanım:** Tek bir IP adresine sahip bir bilgisayarda aynı anda çalışan onlarca farklı ağ uygulamasını (Web tarayıcısı, veritabanı sunucusu, e-posta istemcisi, oyunlar vb.) birbirinden ayıran **16-bitlik (`0` ile `65535` arası)** sanal kapı numaralarıdır. OSI modelinin **Taşıma Katmanında (Transport Layer - Layer 4)** yer alır.
* **Benzetme:** IP adresi apartmanın adresi ise, Port numarası o apartmandaki **dairenin kapı numarasıdır**. Bilgisayara gelen verinin hangi programa teslim edileceğini belirler.
* **Port Aralıkları ve 1989 Portu:**
  * `0 - 1023` (*Well-Known Ports*): Sistem servislerine ayrılmış standart portlardır (HTTP: `80`, HTTPS: `443`, SSH: `22`).
  * `1024 - 49151` (*Registered Ports*): Kullanıcı uygulamaları ve özel sunucular için ayrılmıştır. Bu projede kullanılan **`1989`** numaralı port bu aralıkta yer alır ve sistem servisleriyle çakışmadan güvenli iletişim sağlar.
  * `49152 - 65535` (*Dynamic/Private Ports*): İstemcilerin bağlantı kurarken geçici olarak kullandığı portlardır.

### 4.3. Socket (Soket)
* **Tanım:** Ağ üzerindeki iki program arasında kurulan iki yönlü (*Full-Duplex*) iletişim kanalının yazılım uç noktasıdır (*Endpoint*). Bir **IP Adresi**, bir **Taşıma Protokolü (TCP/UDP)** ve bir **Port Numarasının** birleşmesiyle (`IP:Port`) meydana gelir.
* **Yapısı:** `Socket = IP Adresi + Port Numarası` (Örn: `127.0.0.1:1989`).
* **Çalışma Prensibi:** İşletim sistemi seviyesinde bir dosya tanımlayıcısı (*file descriptor*) gibi çalışır. Uygulama veriyi soketin çıkış akışına (`OutputStream`) yazdığında, işletim sistemi bu veriyi TCP segmentlerine bölerek ağ üzerinden karşı tarafın soketine iletir.
* **Benzetme:** Bina (IP) ve daire (Port) belirlendikten sonra iki daire arasında kurulan **aktif telefon hattı ve ahizesidir**.

---

## 🧱 5. Uygulamada SOLID Prensiplerinin Kullanımı

Yazılım mühendisliğinde tüm kodları tek bir `main` metodunun içine yığmak (spagetti kod) yerine; esnek, test edilebilir ve bakımı kolay bir sistem inşa etmek amacıyla Robert C. Martin tarafından tanımlanan **SOLID** prensipleri bu projede doğrudan uygulanmıştır:

### 5.1. S — Single Responsibility Principle (Tek Sorumluluk Prensibi)
> *"Bir sınıfın (veya modülün) değişmek için yalnızca tek bir nedeni olmalıdır."*

Projede her sınıf yalnızca tek bir işten sorumludur ve görevler tamamen ayrıştırılmıştır:
1. **`WebServer` Sınıfı:** Sadece `ServerSocket` üzerinden `1989` portunu açmak, gelen bağlantıları kabul etmek ve iş parçacıklarını (`Thread`) başlatmakla sorumludur. HTML tasarımının nasıl yapıldığını veya HTTP başlıklarının nasıl yazıldığını bilmez.
2. **`ClientHandler` Sınıfı:** Sadece ağ soketi üzerinden gelen istemci isteğini okumak, HTTP/1.1 yanıt başlıklarını (`200 OK`, `Content-Type` vb.) hazırlamak ve byte akışını sokete yazıp bağlantıyı kapatmakla sorumludur.
3. **`StudentBiographyProvider` Sınıfı:** Sadece öğrenci bilgilerini (H1 boyutunda Ad Soyad, H2 boyutunda Öğrenci No ve Biyografi) içeren biçimlendirilmiş HTML5/CSS3 metnini üretmekle sorumludur. Ağ veya soket işlemleriyle hiçbir ilgisi yoktur.

### 5.2. O — Open/Closed Principle (Açık/Kapalı Prensibi)
> *"Yazılım varlıkları (sınıflar, modüller, fonksiyonlar) gelişime AÇIK, ancak mevcut kodun değiştirilmesine KAPALI olmalıdır."*

* **Uygulamadaki Karşılığı:** İçerik üretimi `ContentProvider` adlı bir `interface` (arayüz) arkasına soyutlanmıştır. İleride sunucunun biyografi sayfası yerine bir "İletişim Sayfası", "JSON API Yanıtı" veya "Sistem Durum Sayfası" döndürmesi istenirse; mevcut `ClientHandler` veya `WebServer` sınıflarının koduna hiç dokunmadan sadece `ContentProvider` arayüzünü uygulayan (`implements ContentProvider`) yeni bir sınıf yazmak yeterlidir.

### 5.3. L — Liskov Substitution Principle (Liskov Yerine Geçme Prensibi)
> *"Türetilen alt sınıflar, türedikleri üst sınıfın (veya arayüzün) yerine, programın davranışını ve doğruluğunu bozmadan geçebilmelidir."*

* **Uygulamadaki Karşılığı:** `ClientHandler` sınıfı yapıcı metodunda (`Constructor`) parametre olarak `ContentProvider` tipinde bir referans alır. Bu referansa `StudentBiographyProvider` nesnesi veya aynı arayüzü uygulayan başka herhangi bir alt sınıf verildiğinde, `contentProvider.getHtmlContent()` çağrısı hiçbir tip dönüşümü (*casting*) veya çalışma zamanı hatası üretmeden beklendiği gibi çalışır.

### 5.4. I — Interface Segregation Principle (Arayüz Ayrımı Prensibi)
> *"İstemciler, kullanmadıkları metotları içeren şişkin arayüzleri uygulamaya (implement etmeye) zorlanmamalıdır."*

* **Uygulamadaki Karşılığı:** Tasarlanan `ContentProvider` arayüzü yalnızca amaca hizmet eden tek bir metot (`String getHtmlContent()`) barındırır. Soket kapatma, port dinleme veya loglama gibi içerik üreticisini ilgilendirmeyen metotlar bu arayüze eklenmemiş, böylece sınıfların gereksiz metotları boş bırakarak uygulaması engellenmiştir.

### 5.5. D — Dependency Inversion Principle (Bağımlılıkların Tersine Çevrilmesi Prensibi)
> *"Üst seviye modüller, alt seviye modüllere doğrudan bağımlı olmamalıdır. Her ikisi de soyutlamalara (interfaces/abstract classes) bağımlı olmalıdır."*

* **Uygulamadaki Karşılığı:** Eğer `ClientHandler` sınıfının içinde doğrudan `new StudentBiographyProvider()` çağrılmış olsaydı, üst seviye ağ yöneticisi alt seviye içerik sınıfına sıkı sıkıya (*tightly coupled*) bağımlı olacaktı. Bunun yerine **Constructor Dependency Injection (Yapıcı Metot ile Bağımlılık Enjeksiyonu)** kullanılmıştır; `ClientHandler` somut sınıfa değil, `ContentProvider` soyutlamasına (interface) bağımlıdır ve içerik sağlayıcı nesneyi dışarıdan parametre olarak alır.

---

## 🎨 6. HTML ve CSS Biçimlendirme Detayları

Ödev yönergesinde belirtilen kriterler doğrultusunda sunucunun döndürdüğü web sayfası aşağıdaki görsel bileşenleri içerir:
* **`<h1>` Etiketi:** Öğrencinin Adı ve Soyadı (`Georgia` serif yazı tipi ve `#1e3799` lacivert renk kodu ile vurgulanmıştır).
* **`<h2>` Etiketi:** Öğrenci Numarası (`#4a69bd` açık mavi tonu ve alt çizgi kenarlığı ile ayrılmıştır).
* **Biyografi Bölümü (`<p>`, `<span>`, `<strong>`):** Eğitim bilgileri, teknik ilgi alanları ve geliştirilen projeleri içeren biyografi metni; modern bir kart (`box-shadow`, `border-radius: 12px`, `background-color: #ffffff`) içerisinde, okunabilir `Segoe UI` font ailesi ve vurgu renkleri (`#eb2f06`) kullanılarak biçimlendirilmiştir.

---

## 🚀 7. Kurulum ve Çalıştırma

Projeyi kendi bilgisayarınızda derlemek ve çalıştırmak için sisteminizde **Java JDK (8 veya üzeri)** kurulu olması yeterlidir:

1. Terminal veya Komut İstemini (CMD) açarak kaynak dosyanın bulunduğu dizine gidin ve kodu derleyin:
   `javac -encoding UTF-8 WebServer.java`
2. Sunucuyu başlatın:
   `java WebServer`
3. Herhangi bir web tarayıcısını açarak aşağıdaki adrese gidin:
   `http://localhost:1989`
