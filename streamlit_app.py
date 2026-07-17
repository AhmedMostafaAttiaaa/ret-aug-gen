import streamlit as st
import requests

API_URL = "http://localhost:8080/api/rag"

st.set_page_config(
    page_title="RAG Chat",
    layout="wide",
    initial_sidebar_state="expanded"
)

# --- Custom CSS: Claude colors + dark antigravity background ---
st.markdown("""
<style>
    /* Main background - deep space dark */
    .stApp {
        background: linear-gradient(160deg, #0d0d0d 0%, #1a1a2e 40%, #16213e 70%, #0d0d0d 100%);
        color: #e8e4df;
    }

    /* Sidebar */
    section[data-testid="stSidebar"] {
        background-color: #1a1a1a !important;
        border-right: 1px solid #2a2a2a;
    }
    section[data-testid="stSidebar"] .stMarkdown {
        color: #c9c2b8;
    }

    /* Hide default streamlit branding */
    #MainMenu {visibility: hidden;}
    footer {visibility: hidden;}
    header {visibility: hidden;}

    /* Chat message containers */
    .user-msg {
        background-color: #2a2a2a;
        border-radius: 12px;
        padding: 14px 18px;
        margin: 8px 0;
        color: #e8e4df;
        max-width: 80%;
        margin-left: auto;
        text-align: right;
        font-size: 15px;
        line-height: 1.6;
    }
    .assistant-msg {
        background-color: #d97706;
        background: linear-gradient(135deg, #b45309 0%, #d97706 100%);
        border-radius: 12px;
        padding: 14px 18px;
        margin: 8px 0;
        color: #fff;
        max-width: 80%;
        font-size: 15px;
        line-height: 1.6;
    }

    /* Input area styling */
    .stTextInput input {
        background-color: #1e1e1e !important;
        color: #e8e4df !important;
        border: 1px solid #3a3a3a !important;
        border-radius: 10px !important;
        padding: 12px 16px !important;
        font-size: 15px !important;
    }
    .stTextInput input:focus {
        border-color: #d97706 !important;
        box-shadow: 0 0 0 1px #d97706 !important;
    }

    /* Buttons */
    .stButton > button {
        background-color: #d97706 !important;
        color: #fff !important;
        border: none !important;
        border-radius: 8px !important;
        padding: 8px 20px !important;
        font-weight: 500 !important;
        transition: background-color 0.2s !important;
    }
    .stButton > button:hover {
        background-color: #b45309 !important;
    }

    /* File uploader */
    .stFileUploader {
        border: 1px dashed #3a3a3a !important;
        border-radius: 10px !important;
    }

    /* Select box */
    .stSelectbox > div > div {
        background-color: #1e1e1e !important;
        color: #e8e4df !important;
        border-color: #3a3a3a !important;
    }

    /* Title styling */
    .main-title {
        font-size: 24px;
        font-weight: 600;
        color: #d97706;
        padding: 10px 0 4px 0;
        letter-spacing: -0.3px;
    }
    .sub-title {
        font-size: 14px;
        color: #666;
        padding-bottom: 16px;
    }

    /* Status messages */
    .status-ok {
        background-color: rgba(34, 197, 94, 0.1);
        border: 1px solid rgba(34, 197, 94, 0.3);
        border-radius: 8px;
        padding: 10px 14px;
        color: #4ade80;
        font-size: 14px;
        margin: 6px 0;
    }
    .status-err {
        background-color: rgba(239, 68, 68, 0.1);
        border: 1px solid rgba(239, 68, 68, 0.3);
        border-radius: 8px;
        padding: 10px 14px;
        color: #f87171;
        font-size: 14px;
        margin: 6px 0;
    }
</style>
""", unsafe_allow_html=True)

# --- Sidebar: Document Upload ---
with st.sidebar:
    st.markdown('<div class="main-title">Documents</div>', unsafe_allow_html=True)
    st.markdown('<div class="sub-title">Upload files to build your knowledge base</div>', unsafe_allow_html=True)

    uploaded_file = st.file_uploader(
        "Drop a file here",
        type=["pdf", "csv", "txt", "docx", "xlsx", "doc"]
    )

    chunking_tech = st.selectbox("Chunking", ("token", "paragraph", "sentence"))

    if st.button("Upload"):
        if uploaded_file is not None:
            with st.spinner("Processing..."):
                try:
                    files = {"file": (uploaded_file.name, uploaded_file.getvalue(), uploaded_file.type)}
                    data = {"chunking": chunking_tech}
                    response = requests.post(f"{API_URL}/upload", files=files, data=data)
                    if response.status_code == 200:
                        st.markdown(f'<div class="status-ok">{response.text}</div>', unsafe_allow_html=True)
                    else:
                        st.markdown(f'<div class="status-err">{response.text}</div>', unsafe_allow_html=True)
                except requests.exceptions.ConnectionError:
                    st.markdown('<div class="status-err">Backend is not running. Start Spring Boot first.</div>', unsafe_allow_html=True)
        else:
            st.markdown('<div class="status-err">Select a file first.</div>', unsafe_allow_html=True)

# --- Main Chat Area ---
st.markdown('<div class="main-title">RAG Chat</div>', unsafe_allow_html=True)
st.markdown('<div class="sub-title">Ask anything about your uploaded documents</div>', unsafe_allow_html=True)

# Session state for chat history
if "messages" not in st.session_state:
    st.session_state.messages = []

# Display chat history
for msg in st.session_state.messages:
    if msg["role"] == "user":
        st.markdown(f'<div class="user-msg">{msg["content"]}</div>', unsafe_allow_html=True)
    else:
        st.markdown(f'<div class="assistant-msg">{msg["content"]}</div>', unsafe_allow_html=True)

# Input
question = st.text_input("Ask a question...")

if st.button("Send"):
    if question:
        st.session_state.messages.append({"role": "user", "content": question})
        with st.spinner(""):
            try:
                response = requests.post(f"{API_URL}/ask", json={"question": question})
                if response.status_code == 200:
                    answer = response.text
                else:
                    answer = f"Error: {response.text}"
            except requests.exceptions.ConnectionError:
                answer = "Backend is not running. Start Spring Boot first."
        st.session_state.messages.append({"role": "assistant", "content": answer})
        st.rerun()
