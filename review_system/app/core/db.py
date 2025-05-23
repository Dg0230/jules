from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker
from app.core.config import settings # Import settings from config.py
from app.models.base import Base # Import Base to create tables

# Define the async engine
engine = create_async_engine(settings.DATABASE_URL, echo=True, future=True)

# Define the async session local
AsyncSessionLocal = sessionmaker(
    bind=engine, class_=AsyncSession, expire_on_commit=False
)

# Dependency to get DB session
async def get_db() -> AsyncSession:
    async with AsyncSessionLocal() as session:
        yield session

# Function to create database tables (useful for initial setup or tests)
# In a production Alembic setup, you'd typically use migrations.
async def create_db_and_tables():
    async with engine.begin() as conn:
        # await conn.run_sync(Base.metadata.drop_all) # Use with caution
        await conn.run_sync(Base.metadata.create_all)
