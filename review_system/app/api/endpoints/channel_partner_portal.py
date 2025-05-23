from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Path, Query

from sqlalchemy.ext.asyncio import AsyncSession

from app.crud import crud_merchant 
from app.schemas import merchant as merchant_schemas 
from app.models.channel_partner import ChannelPartner 
from app.core.db import get_db
from app.schemas.channel_partner import ChannelPartnerFinancialsSummaryResponse # Import the new schema

router = APIRouter()

@router.get("/channel_partners/{channel_partner_id}/merchants/", response_model=List[merchant_schemas.MerchantResponse])
async def read_channel_partner_merchants(
    channel_partner_id: int = Path(..., description="The ID of the channel partner"),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=200),
    db: AsyncSession = Depends(get_db)
):
    # Retrieve a list of merchants associated with a specific channel partner.
    channel_partner = await db.get(ChannelPartner, channel_partner_id)
    if not channel_partner:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="ChannelPartner not found")

    merchants = await crud_merchant.get_merchants_by_channel_partner(
        db, 
        channel_partner_id=channel_partner_id, 
        skip=skip, 
        limit=limit
    )
    return merchants

@router.get("/channel_partners/{channel_partner_id}/financials/", response_model=ChannelPartnerFinancialsSummaryResponse)
async def read_channel_partner_financials_summary(
    channel_partner_id: int = Path(..., description="The ID of the channel partner"),
    db: AsyncSession = Depends(get_db)
):
    # Retrieve basic financial summary for a specific channel partner.
    channel_partner = await db.get(ChannelPartner, channel_partner_id)
    if not channel_partner:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="ChannelPartner not found")

    # The ChannelPartner model directly contains current_balance_for_payout and total_profit_shared.
    # The ChannelPartnerFinancialsSummaryResponse schema is designed to return these.
    # Pydantic will automatically map the fields from the channel_partner ORM model instance.
    return channel_partner
