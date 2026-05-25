# Laboratuvar Sonuçlarını Yorumlayabilen Akıllı Asistanlı Sistem

Hastane lab cihazlarından gelen test sonuçlarını işleyen, doktorların görüntüleyebildiği ve yapay zeka destekli yorum sunan sistem.

Lab cihazını simüle eden mock servis periyodik olarak JSON formatında test sonuçları üretir. Backend bu verileri çekerek doğrular, anormal değerleri tespit eder ve PostgreSQL veritabanına kaydeder. Doktorlar web arayüzü üzerinden sonuçları görüntüleyebilir, filtreleyebilir ve istedikleri sonuç için yapay zeka yorumu alabilir.

---

## 1. Nasıl Kurulur

### Gereksinimler
- Java 17+
- Node.js 18+
- Docker
- Ollama

### 1.1 Repo'yu klonla
```bash
git clone https://github.com/bekazanc/lab-results-assistant.git
cd lab-results-assistant
```

### 1.2 PostgreSQL başlat (Docker)
```bash
docker run --name lab-postgres \
  -e POSTGRES_DB=lab_results \
  -e POSTGRES_USER=labuser \
  -e POSTGRES_PASSWORD=labpass123 \
  -p 5432:5432 \
  -d postgres:15
```

### 1.3 Ollama başlat ve modeli indir
```bash
ollama serve
ollama pull llama3
```

### 1.4 Mock servisi başlat
```bash
cd mock-service
./mvnw spring-boot:run
```

### 1.5 Backend başlat
```bash
cd backend
./mvnw spring-boot:run
```

### 1.6 Frontend başlat
```bash
cd frontend
npm install
npm start
```

### 1.7 Giriş
- URL: http://localhost:3000
- Kullanıcı adı: `doctor`
- Şifre: `doctor123`

---

## 2. Kodun Mimari Yapısı

Sistemin dört bileşeni ve çalışma prensipleri şu şekildedir: 

- 1- Mock servis gerçek lab cihazı gibi davranarak JSON test sonuçları üretir. 
- 2- Backend dakikada bir bu servisten veri çeker, doğruladıktan sonraysa PostgreSQL'e kaydeder. 
- 3- Doktor bir sonuç için yorum istediğinde backend Ollama LLM'e istek atar ve yanıtı DB'ye kaydeder. 
- 4- Doktorun tüm bu verileri görüntülediği web arayüzü ise React frontend kısmıdır.

```
Mock Servis (8081) → Backend (8080) ↔ PostgreSQL (5432)
                          ↕
                    Ollama (11434)
                          ↕
                    Frontend (3000)
```

---

## 3. Yapılanlar ve Gerekçeleri

### 3.1 Mock Servis
Mock servis, backend kısmından bağımsız şekilde gerçek bir lab cihazı gibi davranır ve 8 farklı senaryo üretir.

- NORMAL: Tüm test değerleri referans aralığında

- ABNORMAL_HIGH: Glucose ve HbA1c yüksek (diyabet şüphesi)

- ABNORMAL_LOW: Hemoglobin ve WBC düşük (anemi/enfeksiyon şüphesi)

- CRITICAL: Tüm değerler tehlikeli seviyede (acil müdahale gerekir)

- INVALID_MISSING_FIELDS: patientId veya deviceId eksik (backend reddeder)

- INVALID_EMPTY_TESTS: Test listesi boş (backend reddeder)

- INVALID_NEGATIVE_VALUE: Test değeri negatif (backend reddeder)

- INVALID_DUPLICATE_TEST: Aynı test iki kez gönderilmiş (backend reddeder)

> **Not:** Bu projede klinik önemi yüksek 4 test tipi ele alınmıştır: Glucose (anlık kan şekeri), HbA1c (3 aylık ortalama kan şekeri), Hemoglobin (oksijen taşıma kapasitesi) ve WBC (bağışıklık sistemi göstergesi). Bu testler rutin kan panellerinde yaygın olarak yer alır. Gerçek sistemde test tipleri dinamik olarak tanımlanabilir ve genişletilebilir yapıda olurdu.

> Kaynak: [MedlinePlus — HbA1c Test](https://medlineplus.gov/lab-tests/hemoglobin-a1c-hba1c-test/)

### 3.2 Backend Validasyon Katmanı
Mock servisten gelen her veri şu kontrollere tabi tutuluyor:
- Zorunlu alanlar (patientId, deviceId) eksikse → tüm sonuç reddedilir
- Test listesi boşsa → tüm sonuç reddedilir
- Herhangi bir test değeri negatifse → tüm sonuç reddedilir
- Duplicate test varsa → duplicate atlanır, kalan geçerli testler kaydedilir

### 3.3. Backend Polling — @Scheduled
Backend mock servisten dakikada bir veri çekiyor. Bu projede polling tercih edildi çünkü implementasyonu basit ve mock servis mimarisine uygun. Gerçek sistemde cihaz protokolüne göre WebSocket veya event-driven mimari de kullanılabilir.

> Referans: [HL7 FHIR — Push/Pull Architecture](https://hl7.org/fhir/pushpull.html)

### 3.4 JWT Authentication
Sunucu tarafında durum tutulmaz; kullanıcı bilgisi token içinde şifreli taşınır. BCrypt ile şifreleme, JJWT 0.12.3 ile token üretimi yapılır.

### 3.5 LLM Analiz Geçmişi
LLM yorumu ilk üretildiğinde DB'ye kaydediliyor. Her analiz ayrı tabloda (`lab_result_analyses`) tarihiyle birlikte saklanıyor. Detay sayfasında "Analiz Geçmişi" butonu ile tüm geçmiş görüntülenebilir.

### 3.6 Backend Search — findByPatientIdContainingIgnoreCase
Hasta araması backend'de yapılıyor, bunun için hasta id'lerinin kullanılması gerekmekte. ("6145" yazınca "P-6145" bulunuyor)

### 3.7 Status Filter — findByStatus
CRITICAL/ABNORMAL/NORMAL butonlarıyla backend'de filtreleme yapılıyor, böylece doktorlar ana sayfada sonuçları ayırabiliyorlar. 

### 3.8 Ollama
Ollama tercih edildi çünkü kurulumu tek komutla tamamlanıyor, arka planda servis olarak çalışıyor ve REST API'si backend entegrasyonu için
uyumluydu. 

### 3.9 Docker — PostgreSQL için
PostgreSQL'i doğrudan bilgisayara kurmak yerine Docker container'ı tercih ettim. Tek komutla çalışıyor, projeyi başka birisi kurduğunda da aynı şekilde çalışabilir.

---

## 4. Testler

Projede üç katmanda toplam 38 unit test yazıldı. 
Backend'de 5 test ile validasyon mantığı ve senaryo sınıflandırması test edildi. 
Mock serviste 29 test ile 8 farklı senaryo ve null kontrolleri doğrulandı. 
Frontend'de 4 test ile login sayfasının render edilmesi, hata mesajları ve başarılı giriş akışı test edildi.

---

## 5. Geliştirilebilecek Yanlar

### Role-based yetkilendirme
Şu an tek doktor kullanıcısı var. Admin, doktor, lab teknisyeni gibi roller eklenebilir.

### Her test için ayrı kritik eşik
CRITICAL/ABNORMAL ayrımı şu an mock servisin senaryo adına göre yapılıyor. Gerçek sistemde her testin kendi kritik eşiği tanımlanmalı.

### Refresh token
JWT 24 saat geçerli. Production'da refresh token mekanizması eklenebilir.

### Pagination
Tüm sonuçlar tek seferde çekiliyor. Büyük veri setlerinde `Pageable` ile sayfalama yapılmalı.

### WebSocket ile gerçek zamanlı güncelleme
Şu an doktor sayfayı yenilediğinde yeni sonuçları görüyor. WebSocket ile otomatik güncelleme eklenebilir.

---

## 6. Kullanılan Teknolojiler
- Backend - Spring Boot 4.x
- Frontend - React + TypeScript 
- Veritabanı - PostgreSQL 15
- LLM - Ollama + llama3 
- Auth - JWT (jjwt 0.12.3) 
- Container - Docker 