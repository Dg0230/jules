from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Path

from sqlalchemy.ext.asyncio import AsyncSession

from app.crud import crud_channel_partner
from app.schemas import channel_partner as cp_schemas 
from app.core.db import get_db

router = APIRouter()

@router.post("/", response_model=cp_schemas.ChannelPartnerResponse, status_code=status.HTTP_201_CREATED)
async def admin_create_channel_partner(
    partner_in: cp_schemas.ChannelPartnerCreate, 
    db: AsyncSession = Depends(get_db)
):
    # Admin: Create a new channel partner.
    return await crud_channel_partner.create_channel_partner(db=db, partner_in=partner_in)

@router.get("/", response_model=List[cp_schemas.ChannelPartnerResponse])
async def admin_read_channel_partners(
    skip: int = 0,
    limit: int = 100,
    db: AsyncSession = Depends(get_db)
):
    # Admin: List all channel partners.
    partners = await crud_channel_partner.get_channel_partners(db, skip=skip, limit=limit)
    return partners

@router.get("/{partner_id}", response_model=cp_schemas.ChannelPartnerResponse)
async def admin_read_channel_partner_by_id(
    partner_id: int = Path(..., title="The ID of the channel partner to retrieve"),
    db: AsyncSession = Depends(get_db)
):
    # Admin: Get details of a specific channel partner.
    db_partner = await crud_channel_partner.get_channel_partner(db, partner_id=partner_id, include_merchants=True)
    if db_partner is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="ChannelPartner not found")
    return db_partner

@router.put("/{partner_id}", response_model=cp_schemas.ChannelPartnerResponse)
async def admin_update_channel_partner(
    partner_id: int, 
    partner_in: cp_schemas.ChannelPartnerUpdate, 
    db: AsyncSession = Depends(get_db)
):
    # Admin: Update an existing channel partner's information.
    updated_partner = await crud_channel_partner.update_channel_partner(db, partner_id=partner_id, partner_update=partner_in)
    if updated_partner is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="ChannelPartner not found")
    return updated_partner

@router.delete("/{partner_id}", response_model=cp_schemas.ChannelPartnerResponse)
async def admin_delete_channel_partner(
    partner_id: int, 
    db: AsyncSession = Depends(get_db)
):
    # Admin: Delete a channel partner by ID.
    deleted_partner = await crud_channel_partner.delete_channel_partner(db, partner_id=partner_id)
    if deleted_partner is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="ChannelPartner not found")
    return deleted_partner
