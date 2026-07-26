from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    ai_engine_api_key: str = "not-configured"

    claude_api_key: str = "not-configured"
    claude_model: str = "claude-sonnet-5"

    narajangteo_service_key: str = "not-configured"
    narajangteo_base_url: str = "https://apis.data.go.kr/1230000/ad/BidPublicInfoService"

    embedding_model: str = "paraphrase-multilingual-MiniLM-L12-v2"

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    def is_configured(self, value: str) -> bool:
        return bool(value) and value != "not-configured"


settings = Settings()
