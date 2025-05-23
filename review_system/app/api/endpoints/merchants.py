from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status

from sqlalchemy.ext.asyncio import AsyncSession

from app.crud import crud_merchant # Import the new CRUD module
from app.schemas import merchant as merchant_schemas # Use alias for clarity
from app.core.db import get_db

router = APIRouter()

@router.post("/", response_model=merchant_schemas.MerchantResponse, status_code=status.HTTP_201_CREATED)
async def create_new_merchant(
    merchant_in: merchant_schemas.MerchantCreate, 
    db: AsyncSession = Depends(get_db)
):
    # Docstring: Create a new merchant. Corresponds to Merchant Entry.
    return await crud_merchant.create_merchant(db=db, merchant=merchant_in)

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
