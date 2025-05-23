import pytest
from httpx import AsyncClient
from fastapi import status
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.merchant_financials import MerchantFinancials
from sqlalchemy.future import select

pytestmark = pytest.mark.asyncio

async def test_create_merchant_with_financials(client: AsyncClient, db_session: AsyncSession):
    merchant_data = {"name": "Test Merchant with Financials", "contact_info": "test@example.com"}
    # The client fixture should already have /api/v1 base prefix
    response = await client.post("/merchants/", json=merchant_data) 
    assert response.status_code == status.HTTP_201_CREATED
    created_merchant_data = response.json()
    merchant_id = created_merchant_data["id"]

    # Verify MerchantFinancials was created
    financials_result = await db_session.execute(
        select(MerchantFinancials).filter(MerchantFinancials.merchant_id == merchant_id)
    )
    db_financials = financials_result.scalars().first()
    
    assert db_financials is not None
    assert db_financials.account_balance == 0.00
    assert db_financials.total_top_up_amount == 0.00
    # The merchant API response doesn't include financials, so direct DB check is appropriate.

# We can add other merchant API tests here later, e.g. get, list, update, delete merchants.
# For now, focusing on the financials auto-creation part.

async def test_get_merchant(client: AsyncClient, db_session: AsyncSession):
    # First, create a merchant to fetch
    merchant_data = {"name": "Fetchable Merchant", "contact_info": "fetch@example.com"}
    create_response = await client.post("/merchants/", json=merchant_data)
    assert create_response.status_code == status.HTTP_201_CREATED
    merchant_id = create_response.json()["id"]

    response = await client.get(f"/merchants/{merchant_id}")
    assert response.status_code == status.HTTP_200_OK
    fetched_merchant_data = response.json()
    assert fetched_merchant_data["id"] == merchant_id
    assert fetched_merchant_data["name"] == merchant_data["name"]

async def test_get_non_existent_merchant(client: AsyncClient):
    response = await client.get("/merchants/999999")
    assert response.status_code == status.HTTP_404_NOT_FOUND

async def test_list_merchants(client: AsyncClient, db_session: AsyncSession):
    await client.post("/merchants/", json={"name": "List Merchant 1", "contact_info": "list1@example.com"})
    await client.post("/merchants/", json={"name": "List Merchant 2", "contact_info": "list2@example.com"})

    response = await client.get("/merchants/")
    assert response.status_code == status.HTTP_200_OK
    merchants_list = response.json()
    assert len(merchants_list) >= 2 # Could be more if other tests ran and didn't clean up fully or order of tests

async def test_update_merchant(client: AsyncClient, db_session: AsyncSession):
    create_response = await client.post("/merchants/", json={"name": "Updatable Merchant", "contact_info": "update@example.com"})
    merchant_id = create_response.json()["id"]

    update_data = {"name": "Updated Merchant Name", "contact_info": "updated_contact@example.com"}
    response = await client.put(f"/merchants/{merchant_id}", json=update_data)
    assert response.status_code == status.HTTP_200_OK
    updated_merchant_data = response.json()
    assert updated_merchant_data["name"] == update_data["name"]
    assert updated_merchant_data["contact_info"] == update_data["contact_info"]

async def test_delete_merchant(client: AsyncClient, db_session: AsyncSession):
    create_response = await client.post("/merchants/", json={"name": "Deletable Merchant", "contact_info": "delete@example.com"})
    merchant_id = create_response.json()["id"]

    response = await client.delete(f"/merchants/{merchant_id}")
    assert response.status_code == status.HTTP_200_OK
    
    # Verify it's actually deleted
    get_response = await client.get(f"/merchants/{merchant_id}")
    assert get_response.status_code == status.HTTP_404_NOT_FOUND

    # Verify financials are also deleted (due to cascade)
    financials_result = await db_session.execute(
        select(MerchantFinancials).filter(MerchantFinancials.merchant_id == merchant_id)
    )
    db_financials = financials_result.scalars().first()
    assert db_financials is None
