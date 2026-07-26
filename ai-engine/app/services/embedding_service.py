"""Local text embeddings for evidence-file matching (Phase 5).

Anthropic doesn't offer an embeddings endpoint, so this runs a small
multilingual sentence-transformers model in-process — no extra API key,
no vector-DB infra. Fine at the scale of one company's evidence files
(tens to low hundreds of documents); matching itself is done by the
caller via plain cosine similarity, not an ANN index.
"""

from functools import lru_cache

from app.core.config import settings


@lru_cache(maxsize=1)
def _model():
    from sentence_transformers import SentenceTransformer

    return SentenceTransformer(settings.embedding_model)


def encode(text: str) -> list[float]:
    vector = _model().encode(text, normalize_embeddings=True)
    return vector.tolist()
