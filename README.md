# Laboratuvar Sonuçlarını Yorumlayabilen Akıllı Asistanlı Sistem

Hastane lab cihazlarından gelen test sonuçlarını işleyen, doktorların görüntüleyebildiği ve yapay zeka destekli yorum sunan sistem.

---

## Nasıl Kurulur

### Gereksinimler
- Java 17+
- Node.js 18+
- Docker
- Ollama

### 1. Repo'yu klonla
```bash
git clone https://github.com/bekazanc/lab-results-assistant.git
cd lab-results-assistant
```

### 2. PostgreSQL başlat (Docker)
```bash
docker run --name lab-postgres \
  -e POSTGRES_DB=lab_results \
  -e POSTGRES_USER=labuser \
  -e POSTGRES_PASSWORD=labpass123 \
  -p 5432:5432 \
  -d postgres:15
```

### 3. Ollama başlat ve modeli indir
```bash
ollama serve
ollama pull llama3
```

### 4. Mock servisi başlat
```bash
cd mock-service
./mvnw spring-boot:run
```

### 5. Backend başlat
```bash
cd backend
./mvnw spring-boot:run
```

### 6. Frontend başlat
```bash
cd frontend
npm install
npm start
```

### 7. Giriş
- URL: http://localhost:3000
- Kullanıcı adı: `doctor`
- Şifre: `doctor123`

---

## 🏗️ Mimari