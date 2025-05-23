from logging.config import fileConfig

from sqlalchemy import engine_from_config
from sqlalchemy import pool

from alembic import context

# this is the Alembic Config object, which provides
# access to the values within the .ini file in use.
config = context.config

# Interpret the config file for Python logging.
# This line sets up loggers basically.
if config.config_file_name is not None:
    fileConfig(config.config_file_name)

# --- Start of custom modifications ---
import os
import sys
from dotenv import load_dotenv

# Add project root to sys.path to allow Alembic to find app modules
# This assumes env.py is in alembic/
PROJECT_ROOT = os.path.realpath(os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, PROJECT_ROOT)
# Construct the path to the .env file relative to the PROJECT_ROOT
dotenv_path = os.path.join(PROJECT_ROOT, '.env')
load_dotenv(dotenv_path)


# Import Base and all models for Alembic autogenerate
from app.models.base import Base
from app.models.material import Material, MaterialSet, FileTypeEnum, MaterialStatusEnum
from app.models.merchant import Merchant
from app.models.merchant_financials import MerchantFinancials
from app.models.review import Review, ReviewPlatformEnum

# Ensure sqlalchemy.url is set, prioritizing environment variables if available.
# This makes sure Alembic uses the correct synchronous database URL.
db_url_env = os.environ.get('DATABASE_URL_ALEMBIC', os.environ.get('DATABASE_URL'))
if db_url_env:
    if db_url_env.startswith("postgresql+asyncpg://"): # Convert asyncpg to psycopg2 for Alembic
        db_url_env = db_url_env.replace("postgresql+asyncpg://", "postgresql://")
    config.set_main_option('sqlalchemy.url', db_url_env)
# If DATABASE_URL is not set in .env, Alembic will use the one from alembic.ini

target_metadata = Base.metadata
# --- End of custom modifications ---


# other values from the config, defined by the needs of env.py,
# can be acquired:
# my_important_option = config.get_main_option("my_important_option")
# ... etc.


def run_migrations_offline() -> None:
    """Run migrations in 'offline' mode.

    This configures the context with just a URL
    and not an Engine, though an Engine is acceptable
    here as well.  By skipping the Engine creation
    we don't even need a DBAPI to be available.

    Calls to context.execute() here emit the given string to the
    script output.

    """
    url = config.get_main_option("sqlalchemy.url")
    context.configure(
        url=url,
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
    )

    with context.begin_transaction():
        context.run_migrations()


def run_migrations_online() -> None:
    """Run migrations in 'online' mode.

    In this scenario we need to create an Engine
    and associate a connection with the context.

    """
    connectable = engine_from_config(
        config.get_section(config.config_ini_section, {}),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )

    with connectable.connect() as connection:
        context.configure(
            connection=connection, target_metadata=target_metadata
        )

        with context.begin_transaction():
            context.run_migrations()


if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
