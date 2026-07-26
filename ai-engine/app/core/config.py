from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    ai_engine_api_key: str = "not-configured"

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
