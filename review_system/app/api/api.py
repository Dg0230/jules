from fastapi import APIRouter
from app.api.endpoints import materials
from app.api.endpoints import merchants
from app.api.endpoints import dashboard
from app.api.endpoints import review_tags
from app.api.endpoints import prompt_templates
from app.api.endpoints import reviews # Add this import

api_router = APIRouter()

api_router.include_router(materials.router, prefix="/management", tags=["Material Management"])
api_router.include_router(merchants.router, prefix="/merchants", tags=["Merchant Management"])
api_router.include_router(dashboard.router, tags=["Dashboard"]) 
api_router.include_router(review_tags.router, tags=["Review Tag Management"]) 
api_router.include_router(prompt_templates.router, prefix="/prompt_templates", tags=["Prompt Templates"]) 
api_router.include_router(reviews.router, prefix="/reviews", tags=["Reviews"]) # Add this line
