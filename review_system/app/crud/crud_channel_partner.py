from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy.orm import selectinload

from app.models.channel_partner import ChannelPartner
from app.schemas.channel_partner import ChannelPartnerCreate, ChannelPartnerUpdate

async def create_channel_partner(db: AsyncSession, partner_in: ChannelPartnerCreate) -> ChannelPartner:
    # Create new channel partner.
    db_partner = ChannelPartner(
        name=partner_in.name,
        contact_details=partner_in.contact_details
    )
    db.add(db_partner)
    await db.commit()
    await db.refresh(db_partner)
    return db_partner

async def get_channel_partner(db: AsyncSession, partner_id: int, include_merchants: bool = False) -> Optional[ChannelPartner]:
    # Get single channel partner by ID, optionally including associated merchants.
    query = select(ChannelPartner).filter(ChannelPartner.id == partner_id)
    if include_merchants:
        query = query.options(selectinload(ChannelPartner.merchants))
        
    result = await db.execute(query)
    return result.scalars().first()

async def get_channel_partners(
    db: AsyncSession, 
    skip: int = 0, 
    limit: int = 100
) -> List[ChannelPartner]:
    # Get list of channel partners with pagination.
    query = select(ChannelPartner).order_by(ChannelPartner.name).offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()

async def update_channel_partner(
    db: AsyncSession, 
    partner_id: int, 
    partner_update: ChannelPartnerUpdate
) -> Optional[ChannelPartner]:
    # Update existing channel partner.
    db_partner = await get_channel_partner(db, partner_id) 
    if db_partner is None:
        return None
    
    update_data = partner_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_partner, key, value)
        
    await db.commit()
    await db.refresh(db_partner)
    return db_partner

async def delete_channel_partner(db: AsyncSession, partner_id: int) -> Optional[ChannelPartner]:
    # Delete channel partner by ID.
    db_partner = await get_channel_partner(db, partner_id)
    if db_partner is None:
        return None
    
    await db.delete(db_partner)
    await db.commit()
    return db_partner
