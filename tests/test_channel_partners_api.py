import pytest
from httpx import AsyncClient
from fastapi import status
from decimal import Decimal
from sqlalchemy.ext.asyncio import AsyncSession # Added for db_session type hint

from app.schemas.channel_partner import ChannelPartnerCreate, ChannelPartnerUpdate
from app.schemas.merchant import MerchantCreate
from app.crud import crud_channel_partner, crud_merchant 

pytestmark = pytest.mark.asyncio

# --- Admin API Tests for Channel Partners ---
async def test_admin_create_channel_partner(client: AsyncClient):
    cp_data = {"name": "Test Partner Alpha", "contact_details": "alpha@partner.com"}
    response = await client.post("/admin/channel_partners/", json=cp_data) # /api/v1 prefix is in client fixture
    assert response.status_code == status.HTTP_201_CREATED
    data = response.json()
    assert data["name"] == cp_data["name"]
    assert data["contact_details"] == cp_data["contact_details"]
    assert data["current_balance_for_payout"] == "0.00" 
    assert data["total_profit_shared"] == "0.00"      
    assert "id" in data

async def test_admin_read_channel_partners(client: AsyncClient, db_session: AsyncSession):
    await crud_channel_partner.create_channel_partner(db_session, ChannelPartnerCreate(name="Partner Beta"))
    
    response = await client.get("/admin/channel_partners/")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert isinstance(data, list)
    assert len(data) >= 1
    assert any(p["name"] == "Partner Beta" for p in data)

async def test_admin_read_channel_partner_by_id(client: AsyncClient, db_session: AsyncSession):
    partner = await crud_channel_partner.create_channel_partner(db_session, ChannelPartnerCreate(name="Partner Gamma"))
    
    response = await client.get(f"/admin/channel_partners/{partner.id}")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["id"] == partner.id
    assert data["name"] == "Partner Gamma"
    # Per admin endpoint spec, include_merchants=True by default.
    # If Merchant model was also updated to include channel_partner relationship in its schema,
    # this list could be populated. Assuming MerchantResponse does not include CP by default.
    assert "merchants" not in data # Or assert data["merchants"] == [] if schema includes it.
                                    # The ChannelPartnerResponse schema does not include merchants list.

async def test_admin_update_channel_partner(client: AsyncClient, db_session: AsyncSession):
    partner = await crud_channel_partner.create_channel_partner(db_session, ChannelPartnerCreate(name="Partner Delta"))
    update_data = {
        "name": "Partner Delta Updated", 
        "contact_details": "delta_updated@partner.com",
        "current_balance_for_payout": "123.45" 
    }
    response = await client.put(f"/admin/channel_partners/{partner.id}", json=update_data)
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["name"] == update_data["name"]
    assert data["contact_details"] == update_data["contact_details"]
    assert Decimal(data["current_balance_for_payout"]) == Decimal("123.45")

async def test_admin_delete_channel_partner(client: AsyncClient, db_session: AsyncSession):
    partner = await crud_channel_partner.create_channel_partner(db_session, ChannelPartnerCreate(name="Partner Epsilon"))
    response = await client.delete(f"/admin/channel_partners/{partner.id}")
    assert response.status_code == status.HTTP_200_OK
    
    get_response = await client.get(f"/admin/channel_partners/{partner.id}")
    assert get_response.status_code == status.HTTP_404_NOT_FOUND

# --- Channel Partner Portal API Tests ---
async def test_cp_read_their_merchants(client: AsyncClient, db_session: AsyncSession):
    cp = await crud_channel_partner.create_channel_partner(db_session, ChannelPartnerCreate(name="PortalPartnerA"))
    await crud_merchant.create_merchant(db_session, MerchantCreate(name="Merchant X for CPA", channel_partner_id=cp.id))
    await crud_merchant.create_merchant(db_session, MerchantCreate(name="Merchant Y for CPA", channel_partner_id=cp.id))
    
    other_cp = await crud_channel_partner.create_channel_partner(db_session, ChannelPartnerCreate(name="PortalPartnerB"))
    await crud_merchant.create_merchant(db_session, MerchantCreate(name="Merchant Z for CPB", channel_partner_id=other_cp.id))
    await crud_merchant.create_merchant(db_session, MerchantCreate(name="Unaffiliated Merchant"))

    response = await client.get(f"/channel_partners/{cp.id}/merchants/") # /api/v1 already in client
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert isinstance(data, list)
    assert len(data) == 2
    merchant_names = {m["name"] for m in data}
    assert "Merchant X for CPA" in merchant_names
    assert "Merchant Y for CPA" in merchant_names
    assert "Merchant Z for CPB" not in merchant_names
    assert "Unaffiliated Merchant" not in merchant_names

async def test_cp_read_their_merchants_non_existent_cp(client: AsyncClient):
    response = await client.get("/channel_partners/99999/merchants/")
    assert response.status_code == status.HTTP_404_NOT_FOUND

async def test_cp_read_financials_summary(client: AsyncClient, db_session: AsyncSession):
    cp_data = ChannelPartnerCreate(name="FinancialsPartner")
    partner = await crud_channel_partner.create_channel_partner(db_session, cp_data)
    # Update financials directly for testing this read endpoint
    await crud_channel_partner.update_channel_partner(
        db_session, 
        partner.id, 
        ChannelPartnerUpdate(current_balance_for_payout=Decimal("550.75"), total_profit_shared=Decimal("1200.25"))
    )
    
    response = await client.get(f"/channel_partners/{partner.id}/financials/")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert Decimal(data["current_balance_for_payout"]) == Decimal("550.75")
    assert Decimal(data["total_profit_shared"]) == Decimal("1200.25")

async def test_cp_read_financials_summary_non_existent_cp(client: AsyncClient):
    response = await client.get("/channel_partners/88888/financials/")
    assert response.status_code == status.HTTP_404_NOT_FOUND
