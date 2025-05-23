from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import func, select

from app.models.material import Material, MaterialStatusEnum
from app.models.merchant_financials import MerchantFinancials

from app.crud.crud_review import get_reviews_count_by_merchant
from app.crud.crud_merchant_financials import get_merchant_financials_by_merchant_id

async def get_material_counts_for_merchant(
    db: AsyncSession, 
    merchant_id: int
) -> dict:
    # Calculates total material quantity and remaining (unused) material quantity for a merchant.
    total_query = select(func.count(Material.id)).filter(Material.merchant_id == merchant_id)
    total_result = await db.execute(total_query)
    total_materials = total_result.scalar_one_or_none() or 0

    unused_query = select(func.count(Material.id)).filter(
        Material.merchant_id == merchant_id,
        Material.status == MaterialStatusEnum.UNUSED
    )
    unused_result = await db.execute(unused_query)
    unused_materials = unused_result.scalar_one_or_none() or 0
    
    return {
        "total_material_quantity": total_materials,
        "remaining_material_quantity": unused_materials
    }

async def get_merchant_dashboard_data(
    db: AsyncSession,
    merchant_id: int
) -> dict:
    # Aggregates all necessary data for the merchant dashboard.
    financials = await get_merchant_financials_by_merchant_id(db, merchant_id=merchant_id)
    account_balance = financials.account_balance if financials else 0.00
    total_top_up_amount = financials.total_top_up_amount if financials else 0.00

    positive_review_count = await get_reviews_count_by_merchant(
        db, merchant_id=merchant_id, is_positive=True
    )
    verified_review_count = await get_reviews_count_by_merchant(
        db, merchant_id=merchant_id, is_verified=True, is_positive=True
    )
    
    material_counts = await get_material_counts_for_merchant(db, merchant_id=merchant_id)

    return {
        "positive_review_quantity": positive_review_count,
        "top_up_amount": total_top_up_amount,
        "account_balance": account_balance,
        "verified_review_screenshot_quantity": verified_review_count,
        "total_material_quantity": material_counts["total_material_quantity"],
        "remaining_material_quantity": material_counts["remaining_material_quantity"],
    }
