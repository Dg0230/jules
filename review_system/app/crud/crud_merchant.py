from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

from app.models.merchant import Merchant
from app.models.merchant_financials import MerchantFinancials
from app.models.channel_partner import ChannelPartner 
from app.schemas.merchant import MerchantCreate, MerchantUpdate 

async def create_merchant(db: AsyncSession, merchant_in: MerchantCreate) -> Merchant:
    # Create a new merchant.
    # Optionally associates with a ChannelPartner if channel_partner_id is provided.
    # Creates an associated financials record.
    
    if merchant_in.channel_partner_id is not None:
        channel_partner = await db.get(ChannelPartner, merchant_in.channel_partner_id)
        if not channel_partner:
            raise ValueError(f"ChannelPartner with id {merchant_in.channel_partner_id} not found.")

    merchant_data = merchant_in.dict() 
    db_merchant = Merchant(**merchant_data)
    
    db.add(db_merchant)
    await db.commit() 

    db_financials = MerchantFinancials(merchant_id=db_merchant.id)
    db.add(db_financials)
    
    await db.commit()
    await db.refresh(db_merchant)
    return db_merchant

async def get_merchant(db: AsyncSession, merchant_id: int) -> Optional[Merchant]:
    result = await db.execute(select(Merchant).filter(Merchant.id == merchant_id))
    return result.scalars().first()

async def get_merchants(
    db: AsyncSession, 
    skip: int = 0, 
    limit: int = 100
) -> List[Merchant]:
    query = select(Merchant).offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()

async def update_merchant(
    db: AsyncSession, 
    merchant_id: int, 
    merchant_update: MerchantUpdate
) -> Optional[Merchant]:
    db_merchant = await get_merchant(db, merchant_id)
    if db_merchant is None:
        return None
    
    update_data = merchant_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_merchant, key, value)
        
    await db.commit()
    await db.refresh(db_merchant)
    return db_merchant

async def delete_merchant(db: AsyncSession, merchant_id: int) -> Optional[Merchant]:
    db_merchant = await get_merchant(db, merchant_id)
    if db_merchant is None:
        return None
    
    await db.delete(db_merchant)
    await db.commit()
    return db_merchant

async def get_merchants_by_channel_partner(
    db: AsyncSession,
    channel_partner_id: int,
    skip: int = 0,
    limit: int = 100
) -> List[Merchant]:
    # Get a list of merchants associated with a specific channel_partner_id.
    query = (
        select(Merchant)
        .filter(Merchant.channel_partner_id == channel_partner_id)
        .order_by(Merchant.name) 
        .offset(skip)
        .limit(limit)
    )
    result = await db.execute(query)
    return result.scalars().all()
