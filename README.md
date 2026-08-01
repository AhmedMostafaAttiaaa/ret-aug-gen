# Java RAG with Gemini API

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/java-17-orange.svg)

A Retrieval-Augmented Generation (RAG) system built with Java Spring Boot and Google Gemini API. Upload documents (PDF, CSV, XLSX, TXT, DOCX) and ask questions about their content using natural language.

## Architecture

```
┌─────────────────────┐         HTTP/REST         ┌──────────────────────────┐
│   Streamlit UI      │ ◄──────────────────────►  │   Spring Boot Backend    │
│   (Python)          │      localhost:8080        │   (Java 25)              │
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
| Language            | Java 25                             |
| RAG Framework       | LangChain4j 1.0.0-beta1            |
| LLM                 | Google Gemini / OpenAI / Ollama (local) / Anthropic Claude |
| Embeddings          | Provider-matched embedding model    |
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

- Java 25 (OpenJDK recommended)
- Python 3.8+
- A Google Gemini API key ([Get one here](https://aistudio.google.com/apikey))

## Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd java-rag
   ```

2. **Create a `.env` file** in the project root (copy `.env.example`), choosing an LLM provider with `LLM_PROVIDER` (`gemini`, `openai`, `ollama`, or `anthropic`):

   **Gemini (default)**
   ```
   LLM_PROVIDER=gemini
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

   **OpenAI** (or any OpenAI-compatible API — set `OPENAI_BASE_URL` to point elsewhere)
   ```
   LLM_PROVIDER=openai
   OPENAI_API_KEY=your_openai_api_key_here
   OPENAI_CHAT_MODEL=gpt-4o-mini
   OPENAI_EMBEDDING_MODEL=text-embedding-3-small
   ```

   **Ollama** (local models, no API key — requires `ollama serve` running)
   ```
   LLM_PROVIDER=ollama
   OLLAMA_BASE_URL=http://localhost:11434
   OLLAMA_CHAT_MODEL=llama3.1
   OLLAMA_EMBEDDING_MODEL=nomic-embed-text
   ```

   **Anthropic Claude** (embeddings run locally via all-MiniLM-L6-v2, since Anthropic has no embeddings API)
   ```
   LLM_PROVIDER=anthropic
   ANTHROPIC_API_KEY=your_anthropic_api_key_here
   ANTHROPIC_CHAT_MODEL=claude-sonnet-4-5
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
| GET    | `/api/rag/health`    | Backend + provider status |
| GET    | `/api/rag/provider`  | Active LLM provider info |

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

MIT — see [LICENSE](LICENSE) for details.
