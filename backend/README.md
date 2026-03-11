# EduRAG Backend
### AI-Powered Education Platform — Spring Boot + Gemini + MongoDB

---

## 🏗️ Architecture

```
User (Admin/Student)
     ↓
React Frontend (Optional)
     ↓ REST APIs
Spring Boot Backend
  ├── UploadController      → /api/upload
  ├── AskController         → /api/ask
  └── GenerateController    → /api/generate
     ↓                 ↓
  MongoDB           Gemini API
  (Chunks,          (Embeddings,
  Questions,         Q-Generation,
  Documents)         Doubt Solving)
```

---

## ⚙️ Prerequisites

| Tool        | Version    |
|-------------|------------|
| Java        | 17+        |
| Maven       | 3.8+       |
| MongoDB     | 6.0+       |
| Gemini API  | Key from Google AI Studio |

---

## 🚀 Quick Start

### 1. Clone & Configure

```bash
git clone <your-repo>
cd edurag-backend
```

Edit `src/main/resources/application.properties`:
```properties
gemini.api.key=YOUR_GEMINI_API_KEY_HERE
spring.data.mongodb.uri=mongodb://localhost:27017/edurag
```

Get your Gemini API key from: https://aistudio.google.com/app/apikey

### 2. Start MongoDB

```bash
# Using Docker (easiest)
docker run -d -p 27017:27017 --name mongo mongo:6.0

# Or start local MongoDB
mongod --dbpath /data/db
```

### 3. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

Backend starts at: **http://localhost:8080**

---

## 📡 API Reference

### 1. Upload Study Material
```
POST /api/upload
Content-Type: multipart/form-data

Fields:
  file        → PDF / DOCX / PPTX / TXT (max 50MB)
  subject     → e.g. "Physics"
  chapter     → e.g. "Thermodynamics"
  uploadedBy  → e.g. "admin" (optional, default: "admin")
```

**Response:**
```json
{
  "documentId": "65f1a2b3c4d5e6f7a8b9c0d1",
  "fileName": "physics_ch3.pdf",
  "status": "PROCESSING",
  "message": "File received. Processing and embedding in background."
}
```

**Check Status:**
```
GET /api/upload/status/{documentId}
```

---

### 2. Ask a Question (RAG)
```
POST /api/ask
Content-Type: application/json

{
  "question": "What is the first law of thermodynamics?",
  "subject": "Physics",
  "chapter": "Thermodynamics"
}
```

**Response:**
```json
{
  "question": "What is the first law of thermodynamics?",
  "answer": "The first law of thermodynamics states that energy cannot be created or destroyed...",
  "relevantChunks": ["Energy is conserved in all processes...", "..."],
  "subject": "Physics",
  "chapter": "Thermodynamics"
}
```

---

### 3. Solve a Doubt (Chatbot)
```
POST /api/ask/doubt
Content-Type: application/json

{
  "question": "I don't understand entropy. Can you explain it simply?",
  "subject": "Physics",
  "chapter": "Thermodynamics"
}
```

---

### 4. Generate Questions
```
POST /api/generate
Content-Type: application/json

{
  "subject": "Physics",
  "chapter": "Thermodynamics",
  "numberOfQuestions": 5,
  "difficulty": "MIXED",   // EASY | MEDIUM | HARD | MIXED
  "type": "MIXED"          // MCQ | SHORT_ANSWER | DESCRIPTIVE | MIXED
}
```

**Response:**
```json
{
  "subject": "Physics",
  "chapter": "Thermodynamics",
  "totalGenerated": 5,
  "questions": [
    {
      "id": "65f1a2b3...",
      "questionText": "Which law states that energy is conserved?",
      "type": "MCQ",
      "difficulty": "EASY",
      "answer": "First Law of Thermodynamics",
      "options": ["A. Zeroth Law", "B. First Law", "C. Second Law", "D. Third Law"],
      "correctOption": "B"
    }
  ]
}
```

**Get Stored Questions:**
```
GET /api/generate/questions?subject=Physics&chapter=Thermodynamics&difficulty=EASY
```

**Get Popular Questions (by repeatCount):**
```
GET /api/generate/popular?subject=Physics&chapter=Thermodynamics
```

---

## 🗄️ MongoDB Collections

| Collection  | Description                              |
|-------------|------------------------------------------|
| `documents` | File metadata (name, status, subject...) |
| `chunks`    | Text chunks with embeddings + repeatCount|
| `questions` | Generated Q&A with embeddings            |

---

## 🔧 Configuration Options

```properties
# Chunking
chunking.size=500       # Words per chunk
chunking.overlap=100    # Overlapping words between chunks

# Gemini Models
gemini.model.chat=gemini-1.5-flash
gemini.model.embedding=embedding-001

# Upload
upload.dir=uploads/
spring.servlet.multipart.max-file-size=50MB
```

---

## 📁 Project Structure

```
src/main/java/com/edurag/
├── EduRagApplication.java
├── controller/
│   ├── UploadController.java    # POST /upload
│   ├── AskController.java       # POST /ask, /ask/doubt
│   └── GenerateController.java  # POST /generate
├── service/
│   ├── FileProcessingService.java   # Text extraction + chunking
│   ├── GeminiApiService.java        # Gemini REST client
│   ├── EmbeddingService.java        # Embeddings + similarity search
│   ├── RAGService.java              # RAG orchestration
│   ├── QuestionGenerationService.java
│   └── UploadService.java           # Upload pipeline orchestrator
├── model/
│   ├── EduDocument.java
│   ├── TextChunk.java
│   └── Question.java
├── repository/
│   ├── DocumentRepository.java
│   ├── ChunkRepository.java
│   └── QuestionRepository.java
├── dto/
│   └── ApiDtos.java
└── config/
    ├── AsyncConfig.java
    ├── MongoConfig.java
    └── GlobalExceptionHandler.java
```

---

## 🧪 Testing with curl

```bash
# 1. Upload a PDF
curl -X POST http://localhost:8080/api/upload \
  -F "file=@physics.pdf" \
  -F "subject=Physics" \
  -F "chapter=Thermodynamics"

# 2. Ask a question
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"What is entropy?","subject":"Physics","chapter":"Thermodynamics"}'

# 3. Generate 5 MCQs
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{"subject":"Physics","chapter":"Thermodynamics","numberOfQuestions":5,"difficulty":"MEDIUM","type":"MCQ"}'
```

---

## 👥 Team Division Suggestion

| Member | Module |
|--------|--------|
| A | FileProcessingService + UploadController |
| B | GeminiApiService + EmbeddingService |
| C | RAGService + AskController |
| D | QuestionGenerationService + GenerateController |
