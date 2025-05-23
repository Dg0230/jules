from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.crud.crud_dashboard import get_merchant_dashboard_data
from app.schemas.dashboard import MerchantDashboardResponse
from app.core.db import get_db
from app.models.merchant import Merchant 

router = APIRouter()

@router.get("/merchants/{merchant_id}/dashboard", response_model=MerchantDashboardResponse)
async def read_merchant_dashboard(
    merchant_id: int,
    db: AsyncSession = Depends(get_db)
):
    # Docstring: Retrieve aggregated data for the merchant dashboard.
    merchant = await db.get(Merchant, merchant_id)
    if not merchant:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Merchant not found")

    dashboard_data = await get_merchant_dashboard_data(db, merchant_id=merchant_id)
    return dashboard_data
