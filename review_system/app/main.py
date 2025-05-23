from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
import logging

# Basic logging setup
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Review Management System")

# Placeholder for DB connection events (will be expanded)
@app.on_event("startup")
async def startup_event():
    logger.info("Application startup...")
    # In a real setup, you'd initialize DB connections here
from app.core.db import create_db_and_tables, engine # Updated import
    # await create_db_and_tables() # If using SQLAlchemy's create_all for initial setup
    logger.info("Database connection placeholder.")
    # This will create tables based on SQLAlchemy models if they don't exist.
    # For production, Alembic migrations are preferred for schema management.
    await create_db_and_tables() 
    logger.info("Database tables checked/created.")


@app.on_event("shutdown")
async def shutdown_event():
    logger.info("Application shutdown...")
    # In a real setup, you'd close DB connections here
    await engine.dispose() # Dispose of the engine connection pool
    logger.info("Database connection pool closed.")

# Basic root endpoint
@app.get("/")
async def root():
    return {"message": "Welcome to the Review Management System"}

# Basic error handler example
@app.exception_handler(Exception)
async def generic_exception_handler(request: Request, exc: Exception):
    logger.error(f"Unhandled exception: {exc}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"detail": "An internal server error occurred."},
    )

# Placeholder for API routers (to be added later)
from app.api.api import api_router # Import the main api router
app.include_router(api_router, prefix="/api/v1") # Prefix for all v1 APIs


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
