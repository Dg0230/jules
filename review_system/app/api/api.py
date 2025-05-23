from fastapi import APIRouter
from app.api.endpoints import materials
from app.api.endpoints import merchants
from app.api.endpoints import dashboard # Add this import

api_router = APIRouter()

api_router.include_router(materials.router, prefix="/management", tags=["Material Management"])
api_router.include_router(merchants.router, prefix="/merchants", tags=["Merchant Management"])
api_router.include_router(dashboard.router, tags=["Dashboard"]) # Add this line
