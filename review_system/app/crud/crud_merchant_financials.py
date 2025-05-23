from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import update as sqlalchemy_update # To avoid conflict with schema name
from decimal import Decimal # For handling numeric/decimal types

from app.models.merchant import Merchant # Needed to ensure merchant exists
from app.models.merchant_financials import MerchantFinancials
from app.schemas.merchant_financials import MerchantFinancialsCreate, MerchantFinancialsUpdate

async def create_merchant_financials(
    db: AsyncSession, 
    financials_in: MerchantFinancialsCreate
) -> MerchantFinancials:
    # Create a new merchant financials record.
    # This is typically called when a new merchant is created or financials are initialized.
    merchant = await db.get(Merchant, financials_in.merchant_id)
    if not merchant:
        raise ValueError(f"Merchant with id {financials_in.merchant_id} not found.")

    existing_financials = await get_merchant_financials_by_merchant_id(db, financials_in.merchant_id)
    if existing_financials:
        raise ValueError(f"Financials already exist for merchant id {financials_in.merchant_id}.")

    db_financials = MerchantFinancials(**financials_in.dict())
    db.add(db_financials)
    await db.commit()
    await db.refresh(db_financials)
    return db_financials

async def get_merchant_financials(
    db: AsyncSession, 
    financials_id: int
) -> Optional[MerchantFinancials]:
    # Get a merchant financials record by its own ID.
    result = await db.execute(select(MerchantFinancials).filter(MerchantFinancials.id == financials_id))
    return result.scalars().first()

async def get_merchant_financials_by_merchant_id(
    db: AsyncSession, 
    merchant_id: int
) -> Optional[MerchantFinancials]:
    # Get a merchant financials record by merchant_id.
    result = await db.execute(select(MerchantFinancials).filter(MerchantFinancials.merchant_id == merchant_id))
    return result.scalars().first()

async def update_merchant_financials(
    db: AsyncSession, 
    merchant_id: int, 
    financials_update: MerchantFinancialsUpdate
) -> Optional[MerchantFinancials]:
    # Update merchant financials.
    # This is a generic update. Specific operations like top-up might have dedicated functions.
    db_financials = await get_merchant_financials_by_merchant_id(db, merchant_id)
    if db_financials is None:
        return None 

    update_data = financials_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        if value is not None: 
            setattr(db_financials, key, Decimal(str(value)))
        
    await db.commit()
    await db.refresh(db_financials)
    return db_financials

async def record_top_up(
    db: AsyncSession, 
    merchant_id: int, 
    amount: Decimal
) -> Optional[MerchantFinancials]:
    # Records a top-up for a merchant.
    # Increases total_top_up_amount and account_balance.
    # Creates MerchantFinancials if it doesn't exist for the merchant.
    if amount <= Decimal("0"):
        raise ValueError("Top-up amount must be positive.")

    merchant = await db.get(Merchant, merchant_id)
    if not merchant:
        raise ValueError(f"Merchant with id {merchant_id} not found for top-up.")

    db_financials = await get_merchant_financials_by_merchant_id(db, merchant_id)

    if db_financials is None:
        db_financials = MerchantFinancials(
            merchant_id=merchant_id,
            account_balance=amount,
            total_top_up_amount=amount
        )
        db.add(db_financials)
    else:
        db_financials.account_balance += amount
        db_financials.total_top_up_amount += amount
        
    await db.commit()
    await db.refresh(db_financials)
    return db_financials
