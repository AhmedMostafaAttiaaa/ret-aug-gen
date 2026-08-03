# Changelog

## Unreleased
- Added support for OpenAI, Ollama, and Anthropic Claude as alternative LLM providers alongside Gemini, selectable via `LLM_PROVIDER`.
- Added `/api/rag/provider` endpoint to report the active provider and readiness.
- Added MIT license, `.editorconfig`, and `.gitattributes`.
- Added streaming responses via `/api/rag/ask-stream`, consumed by the Streamlit UI.
- Persisted the embedding store to disk so uploads survive a backend restart.
- Added unit and MockMvc tests for `DocumentService` and `RagController`.
- Rejects empty file uploads and blank/whitespace-only paths and questions instead of failing later.
- Made the HTTP port configurable via `SERVER_PORT`.
