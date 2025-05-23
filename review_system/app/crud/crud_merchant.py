from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

from app.models.merchant import Merchant
from app.models.merchant_financials import MerchantFinancials # Add this import
from app.schemas.merchant import MerchantCreate, MerchantUpdate

async def create_merchant(db: AsyncSession, merchant: MerchantCreate) -> Merchant:
    # Create a new merchant and its associated financials record.
    db_merchant = Merchant(name=merchant.name, contact_info=merchant.contact_info)
    db.add(db_merchant)
    await db.commit() 

    # Create an initial financials record for the new merchant
    db_financials = MerchantFinancials(merchant_id=db_merchant.id) 
    db.add(db_financials)
    
    await db.commit()
    await db.refresh(db_merchant)
    # To access financials after refresh: await db.refresh(db_merchant, attribute_names=['financials'])
    # or ensure the relationship is eagerly loaded if needed immediately.
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
    
    # The MerchantFinancials record will be deleted due to cascade="all, delete-orphan"
    await db.delete(db_merchant)
    await db.commit()
    return db_merchant
