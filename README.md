# Java RAG with Gemini API

A Retrieval-Augmented Generation (RAG) system built with Java Spring Boot and Google Gemini API. Upload documents (PDF, CSV, XLSX, TXT, DOCX) and ask questions about their content using natural language.

## Architecture

```
┌─────────────────────┐         HTTP/REST         ┌──────────────────────────┐
│   Streamlit UI      │ ◄──────────────────────►  │   Spring Boot Backend    │
│   (Python)          │      localhost:8080        │   (Java 17)              │
│   Port: 8501        │                           │                          │
└─────────────────────┘                           │  ┌────────────────────┐  │
                                                  │  │  Apache Tika       │  │
                                                  │  │  (Document Parser) │  │
                                                  │  └────────────────────┘  │
                                                  │  ┌────────────────────┐  │
                                                  │  │  LangChain4j       │  │
                                                  │  │  (RAG Orchestrator)│  │
                                                  │  └────────────────────┘  │
                                                  │  ┌────────────────────┐  │
                                                  │  │  Gemini API        │  │
                                                  │  │  (LLM + Embeddings)│  │
                                                  │  └────────────────────┘  │
                                                  └──────────────────────────┘
```

## Tech Stack

| Component           | Technology                          |
|---------------------|-------------------------------------|
| Backend Framework   | Spring Boot 3.2.5                   |
| Language            | Java 17                             |
| RAG Framework       | LangChain4j 1.0.0-beta1            |
| LLM                 | Google Gemini 1.5 Flash             |
| Embeddings          | Google text-embedding-004           |
| Document Parsing    | Apache Tika                         |
| Vector Store        | In-Memory Embedding Store           |
| Frontend            | Streamlit (Python)                  |
| Build Tool          | Maven                               |

## Features

- **Multi-format document upload**: PDF, CSV, XLSX, TXT, DOCX
- **Multiple chunking strategies**: Token-based, paragraph-based, sentence-based
- **RAG pipeline**: Document ingestion, embedding, retrieval, and generation
- **Chat interface**: Conversational Q&A with chat memory (10 messages)
- **REST API**: Full API for programmatic access

## Prerequisites

- Java 17 (OpenJDK recommended)
- Python 3.8+
- A Google Gemini API key ([Get one here](https://aistudio.google.com/apikey))

## Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd java-rag
   ```

2. **Create a `.env` file** in the project root:
   ```
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

3. **Install Python dependencies**
   ```bash
   pip install streamlit requests
   ```

## Running

1. **Start the Java backend** (Terminal 1):
   ```bash
   ./run-backend.bat
   ```

2. **Start the Streamlit frontend** (Terminal 2):
   ```bash
   streamlit run streamlit_app.py
   ```

3. Open your browser at `http://localhost:8501`

## API Endpoints

| Method | Endpoint             | Description              |
|--------|----------------------|--------------------------|
| POST   | `/api/rag/upload`    | Upload a file            |
| POST   | `/api/rag/load-path` | Load file from disk path |
| POST   | `/api/rag/ask`       | Ask a question           |

## Project Structure

```
java-rag/
├── src/main/java/com/example/raggemini/
│   ├── RagGeminiApplication.java        # Spring Boot entry point
│   ├── controller/
│   │   └── RagController.java           # REST API endpoints
│   └── service/
│       ├── DocumentService.java         # Document loading & chunking
│       └── RagService.java              # RAG pipeline (Gemini + embeddings)
├── streamlit_app.py                     # Frontend UI
├── pom.xml                              # Maven dependencies
├── run-backend.bat                      # Backend launch script
├── .env                                 # API keys (not in git)
└── .gitignore
```

## License

MIT
