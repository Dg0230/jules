from pydantic import BaseSettings

class Settings(BaseSettings):
    DATABASE_URL: str = "postgresql+asyncpg://user:password@host:port/dbname"
    # Add other settings as needed

    class Config:
        env_file = ".env"

settings = Settings()
