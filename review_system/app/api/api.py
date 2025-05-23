from fastapi import APIRouter
from app.api.endpoints import materials

api_router = APIRouter()
api_router.include_router(materials.router, prefix="/management", tags=["Material Management"]) 
# Using "management" as a prefix for these merchant-facing material endpoints
