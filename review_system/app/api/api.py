from fastapi import APIRouter
from app.api.endpoints import materials
from app.api.endpoints import merchants # Add this import

api_router = APIRouter()

# Include material management routes
api_router.include_router(materials.router, prefix="/management", tags=["Material Management"])

# Include merchant management routes
api_router.include_router(merchants.router, prefix="/merchants", tags=["Merchant Management"]) # Add this line
