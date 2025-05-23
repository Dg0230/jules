import pytest
from decimal import Decimal
from sqlalchemy.ext.asyncio import AsyncSession
from app.crud import crud_merchant, crud_merchant_financials
from app.schemas.merchant import MerchantCreate
from app.models.merchant_financials import MerchantFinancials # For direct checks
from sqlalchemy.future import select


pytestmark = pytest.mark.asyncio

async def test_record_top_up_existing_merchant(db_session: AsyncSession):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="FinTest Merchant", contact_info="fintest@example.com"))
    
    amount1 = Decimal("100.50")
    financials1 = await crud_merchant_financials.record_top_up(db_session, merchant_id=merchant.id, amount=amount1)
    assert financials1 is not None
    assert financials1.account_balance == amount1
    assert financials1.total_top_up_amount == amount1

    amount2 = Decimal("50.25")
    financials2 = await crud_merchant_financials.record_top_up(db_session, merchant_id=merchant.id, amount=amount2)
    assert financials2 is not None
    assert financials2.account_balance == amount1 + amount2
    assert financials2.total_top_up_amount == amount1 + amount2

async def test_record_top_up_for_merchant_without_financials_record(db_session: AsyncSession):
    # This case also tests if record_top_up creates financials if none exist
    # Standard merchant creation in crud_merchant now auto-creates financials.
    # To test this specific path of record_top_up, we'd need a merchant without one.
    # For this test, we assume crud_merchant.create_merchant works as intended and creates financials.
    # So, this test is similar to test_record_top_up_existing_merchant.
    # If we wanted to force a merchant without financials, we'd have to manually delete it or
    # use a different merchant creation path not available in current CRUD.
    
    merchant_data = {"name": "NewFin Merchant", "contact_info": "newfin@example.com"}
    # Manually create merchant without calling our overridden crud_merchant.create_merchant
    # to avoid auto-financials creation for this specific test case.
    from app.models.merchant import Merchant as MerchantModel
    db_merchant_obj = MerchantModel(**merchant_data)
    db_session.add(db_merchant_obj)
    await db_session.commit()
    await db_session.refresh(db_merchant_obj)
    merchant_id = db_merchant_obj.id

    # Verify no financials exist yet
    stmt_check = select(MerchantFinancials).filter(MerchantFinancials.merchant_id == merchant_id)
    result_check = await db_session.execute(stmt_check)
    initial_financials = result_check.scalars().first()
    assert initial_financials is None

    # Now record top-up
    amount = Decimal("75.00")
    financials = await crud_merchant_financials.record_top_up(db_session, merchant_id=merchant_id, amount=amount)
    assert financials is not None
    assert financials.account_balance == amount
    assert financials.total_top_up_amount == amount


async def test_record_top_up_non_existent_merchant(db_session: AsyncSession):
    with pytest.raises(ValueError, match="Merchant with id 99999 not found for top-up."):
        await crud_merchant_financials.record_top_up(db_session, merchant_id=99999, amount=Decimal("10.00"))

async def test_record_top_up_invalid_amount(db_session: AsyncSession):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="InvalidAmount Merchant", contact_info="invalid@example.com"))
    with pytest.raises(ValueError, match="Top-up amount must be positive."):
        await crud_merchant_financials.record_top_up(db_session, merchant_id=merchant.id, amount=Decimal("-10.00"))
    with pytest.raises(ValueError, match="Top-up amount must be positive."):
        await crud_merchant_financials.record_top_up(db_session, merchant_id=merchant.id, amount=Decimal("0.00"))

async def test_get_merchant_financials_by_merchant_id(db_session: AsyncSession):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="GetFin Merchant", contact_info="getfin@example.com"))
    # Financials are auto-created, let's record a top-up to make them non-zero
    await crud_merchant_financials.record_top_up(db_session, merchant_id=merchant.id, amount=Decimal("123.45"))

    retrieved_financials = await crud_merchant_financials.get_merchant_financials_by_merchant_id(db_session, merchant_id=merchant.id)
    assert retrieved_financials is not None
    assert retrieved_financials.merchant_id == merchant.id
    assert retrieved_financials.account_balance == Decimal("123.45")

async def test_get_merchant_financials_for_non_existent_merchant(db_session: AsyncSession):
    retrieved_financials = await crud_merchant_financials.get_merchant_financials_by_merchant_id(db_session, merchant_id=88888)
    assert retrieved_financials is None

async def test_update_merchant_financials(db_session: AsyncSession):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="UpdateFin Merchant", contact_info="updatefin@example.com"))
    initial_financials = await crud_merchant_financials.get_merchant_financials_by_merchant_id(db_session, merchant.id)
    assert initial_financials.account_balance == Decimal("0.00") # Default

    from app.schemas.merchant_financials import MerchantFinancialsUpdate
    update_schema = MerchantFinancialsUpdate(account_balance=Decimal("500.75"), total_top_up_amount=Decimal("1000.00"))
    
    updated_financials = await crud_merchant_financials.update_merchant_financials(
        db_session, 
        merchant_id=merchant.id, 
        financials_update=update_schema
    )
    assert updated_financials is not None
    assert updated_financials.account_balance == Decimal("500.75")
    assert updated_financials.total_top_up_amount == Decimal("1000.00") # Assuming direct update is allowed by this function for testing

async def test_update_merchant_financials_partial(db_session: AsyncSession):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="PartialUpdateFin Merchant", contact_info="partial@example.com"))
    await crud_merchant_financials.record_top_up(db_session, merchant_id=merchant.id, amount=Decimal("100.00")) # Initial state

    from app.schemas.merchant_financials import MerchantFinancialsUpdate
    update_schema = MerchantFinancialsUpdate(account_balance=Decimal("150.50")) # Only updating balance
    
    updated_financials = await crud_merchant_financials.update_merchant_financials(
        db_session, 
        merchant_id=merchant.id, 
        financials_update=update_schema
    )
    assert updated_financials is not None
    assert updated_financials.account_balance == Decimal("150.50")
    assert updated_financials.total_top_up_amount == Decimal("100.00") # Should remain unchanged
