from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status # Ensured all imports

from sqlalchemy.ext.asyncio import AsyncSession

from app.crud import crud_merchant # Ensured crud_merchant is imported
from app.schemas import merchant as merchant_schemas # Ensured schemas are imported
from app.core.db import get_db # Ensured get_db is imported

router = APIRouter() # This should already exist

@router.post("/", response_model=merchant_schemas.MerchantResponse, status_code=status.HTTP_201_CREATED)
async def create_new_merchant( # Function updated
    merchant_in: merchant_schemas.MerchantCreate, 
    db: AsyncSession = Depends(get_db)
):
    # Create a new merchant.
    # Optionally associates with a ChannelPartner if channel_partner_id is provided.
    try:
        return await crud_merchant.create_merchant(db=db, merchant=merchant_in)
    except ValueError as e: 
        # Catch ValueError from CRUD (e.g., ChannelPartner not found)
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))

@router.get("/{merchant_id}", response_model=merchant_schemas.MerchantResponse)
async def read_merchant_by_id(
    merchant_id: int, 
    db: AsyncSession = Depends(get_db)
):
    # Docstring: Retrieve a single merchant by its ID.
    db_merchant = await crud_merchant.get_merchant(db, merchant_id=merchant_id)
    if db_merchant is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Merchant not found")
    return db_merchant

@router.get("/", response_model=List[merchant_schemas.MerchantResponse])
async def read_all_merchants(
    skip: int = 0, 
    limit: int = 100, 
    db: AsyncSession = Depends(get_db)
):
    # Docstring: Retrieve a list of merchants with pagination.
    merchants = await crud_merchant.get_merchants(db, skip=skip, limit=limit)
    return merchants

@router.put("/{merchant_id}", response_model=merchant_schemas.MerchantResponse)
async def update_existing_merchant(
    merchant_id: int, 
    merchant_in: merchant_schemas.MerchantUpdate, 
    db: AsyncSession = Depends(get_db)
):
    # Docstring: Update an existing merchant's information. Corresponds to Merchant info editing and management.
    db_merchant = await crud_merchant.update_merchant(db, merchant_id=merchant_id, merchant_update=merchant_in)
    if db_merchant is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Merchant not found")
    return db_merchant

@router.delete("/{merchant_id}", response_model=merchant_schemas.MerchantResponse)
async def delete_existing_merchant(
    merchant_id: int, 
    db: AsyncSession = Depends(get_db)
):
    # Docstring: Delete a merchant by ID.
    db_merchant = await crud_merchant.delete_merchant(db, merchant_id=merchant_id)
    if db_merchant is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Merchant not found")
    return db_merchant
