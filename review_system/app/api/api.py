from fastapi import APIRouter
from app.api.endpoints import materials
from app.api.endpoints import merchants
from app.api.endpoints import dashboard
from app.api.endpoints import review_tags
from app.api.endpoints import prompt_templates
from app.api.endpoints import reviews 
from app.api.endpoints import admin_channel_partners
from app.api.endpoints import channel_partner_portal # Add this import

api_router = APIRouter() 

api_router.include_router(materials.router, prefix="/management", tags=["Material Management"])
api_router.include_router(merchants.router, prefix="/merchants", tags=["Merchant Management"])
api_router.include_router(dashboard.router, tags=["Dashboard"]) 
api_router.include_router(review_tags.router, tags=["Review Tag Management"]) 
api_router.include_router(prompt_templates.router, prefix="/prompt_templates", tags=["Prompt Templates"]) 
api_router.include_router(reviews.router, prefix="/reviews", tags=["Reviews"])
api_router.include_router(
    admin_channel_partners.router, 
    prefix="/admin/channel_partners", 
    tags=["Admin: Channel Partner Management"]
)
api_router.include_router(
    channel_partner_portal.router,
    tags=["Channel Partner Portal"] 
)
