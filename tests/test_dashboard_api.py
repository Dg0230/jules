import pytest
from httpx import AsyncClient
from fastapi import status
from decimal import Decimal
from sqlalchemy.ext.asyncio import AsyncSession # Added for db_session type hint

from app.schemas.merchant import MerchantCreate
from app.schemas.review import ReviewCreate, ReviewPlatformEnum
from app.schemas.material import MaterialCreate, FileTypeEnum, MaterialUpdate, MaterialStatusEnum
from app.crud import crud_merchant, crud_review, crud_material, crud_merchant_financials

pytestmark = pytest.mark.asyncio

async def test_get_merchant_dashboard_data(client: AsyncClient, db_session: AsyncSession):
    # 1. Create Merchant and initial data
    # Using crud_merchant.create_merchant will also create initial financials record.
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="Dashboard Merchant", contact_info="dash@example.com"))
    merchant_id = merchant.id

    # 2. Record Top-ups
    await crud_merchant_financials.record_top_up(db_session, merchant_id=merchant_id, amount=Decimal("200.00"))
    await crud_merchant_financials.record_top_up(db_session, merchant_id=merchant_id, amount=Decimal("50.00")) # Total 250

    # 3. Create Materials
    await crud_material.create_material(db_session, MaterialCreate(merchant_id=merchant_id, content_url="http://example.com/m1.jpg", file_type=FileTypeEnum.IMAGE))
    await crud_material.create_material(db_session, MaterialCreate(merchant_id=merchant_id, content_url="http://example.com/m2.jpg", file_type=FileTypeEnum.IMAGE))
    material3 = await crud_material.create_material(db_session, MaterialCreate(merchant_id=merchant_id, content_url="http://example.com/m3.txt", file_type=FileTypeEnum.TEXT))
    # Mark one material as used
    await crud_material.update_material(db_session, material_id=material3.id, material_update=MaterialUpdate(status=MaterialStatusEnum.USED))


    # 4. Create Reviews
    await crud_review.create_review(db_session, ReviewCreate(merchant_id=merchant_id, platform=ReviewPlatformEnum.MEITUAN, is_positive=True, is_verified=True))
    await crud_review.create_review(db_session, ReviewCreate(merchant_id=merchant_id, platform=ReviewPlatformEnum.CTRIP, is_positive=True, is_verified=False))
    await crud_review.create_review(db_session, ReviewCreate(merchant_id=merchant_id, platform=ReviewPlatformEnum.OTHER, is_positive=False, is_verified=True)) # Not positive, but verified

    # 5. Call Dashboard API
    response = await client.get(f"/merchants/{merchant_id}/dashboard") # Base URL /api/v1 is in client fixture
    assert response.status_code == status.HTTP_200_OK
    
    data = response.json()
    # Values based on crud_dashboard.get_merchant_dashboard_data logic
    assert data["positive_review_quantity"] == 2 # (is_positive=True)
    assert Decimal(data["top_up_amount"]) == Decimal("250.00")
    assert Decimal(data["account_balance"]) == Decimal("250.00") 
    # verified_review_screenshot_quantity logic in crud_dashboard is: is_verified=True, is_positive=True
    assert data["verified_review_screenshot_quantity"] == 1 
    assert data["total_material_quantity"] == 3
    assert data["remaining_material_quantity"] == 2 # Two are UNUSED by default, one was set to USED

async def test_get_merchant_dashboard_non_existent_merchant(client: AsyncClient):
    response = await client.get("/merchants/99999/dashboard")
    assert response.status_code == status.HTTP_404_NOT_FOUND

async def test_get_merchant_dashboard_new_merchant_no_activity(client: AsyncClient, db_session: AsyncSession):
    # Test dashboard for a merchant with no reviews, materials, or additional top-ups after creation
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="New NoActivity Merchant", contact_info="newdash@example.com"))
    merchant_id = merchant.id
    
    response = await client.get(f"/merchants/{merchant_id}/dashboard")
    assert response.status_code == status.HTTP_200_OK
    
    data = response.json()
    assert data["positive_review_quantity"] == 0
    assert Decimal(data["top_up_amount"]) == Decimal("0.00") # Initial financials are 0
    assert Decimal(data["account_balance"]) == Decimal("0.00") # Initial financials are 0
    assert data["verified_review_screenshot_quantity"] == 0
    assert data["total_material_quantity"] == 0
    assert data["remaining_material_quantity"] == 0
