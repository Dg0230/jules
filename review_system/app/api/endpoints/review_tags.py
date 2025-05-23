from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Path

from sqlalchemy.ext.asyncio import AsyncSession

from app.crud import crud_review_tag, crud_custom_ai_prompt # Added crud_custom_ai_prompt
from app.schemas import review_tag as review_tag_schemas 
from app.schemas import custom_ai_prompt as custom_ai_prompt_schemas # Added custom_ai_prompt_schemas
from app.core.db import get_db
from app.models.merchant import Merchant 

router = APIRouter()

@router.post("/review_tags/", response_model=review_tag_schemas.ReviewTagResponse, status_code=status.HTTP_201_CREATED)
async def create_new_review_tag(
    review_tag_in: review_tag_schemas.ReviewTagCreate, 
    db: AsyncSession = Depends(get_db)
):
    # Create a new review tag
    try:
        return await crud_review_tag.create_review_tag(db=db, review_tag_in=review_tag_in)
    except ValueError as e: 
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))

@router.get("/merchants/{merchant_id}/review_tags/", response_model=List[review_tag_schemas.ReviewTagResponse])
async def read_review_tags_for_merchant(
    merchant_id: int = Path(..., title="The ID of the merchant whose review tags to retrieve"),
    skip: int = 0,
    limit: int = 100,
    db: AsyncSession = Depends(get_db)
):
    # List all review tags for a specific merchant.
    merchant = await db.get(Merchant, merchant_id)
    if not merchant:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Merchant with id {merchant_id} not found.")
        
    review_tags = await crud_review_tag.get_review_tags_by_merchant(
        db, merchant_id=merchant_id, skip=skip, limit=limit
    )
    return review_tags

@router.get("/review_tags/{tag_id}", response_model=review_tag_schemas.ReviewTagResponse)
async def read_review_tag_by_id(
    tag_id: int = Path(..., title="The ID of the review tag to retrieve"),
    db: AsyncSession = Depends(get_db)
):
    # Get details of a specific review tag.
    db_review_tag = await crud_review_tag.get_review_tag(db, review_tag_id=tag_id)
    if db_review_tag is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="ReviewTag not found")
    return db_review_tag

@router.put("/review_tags/{tag_id}", response_model=review_tag_schemas.ReviewTagResponse)
async def update_existing_review_tag(
    tag_id: int, 
    review_tag_in: review_tag_schemas.ReviewTagUpdate, 
    db: AsyncSession = Depends(get_db)
):
    # Update an existing review tag.
    try:
        updated_tag = await crud_review_tag.update_review_tag(db, review_tag_id=tag_id, review_tag_update=review_tag_in)
        if updated_tag is None:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="ReviewTag not found")
        return updated_tag
    except ValueError as e: 
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))

@router.delete("/review_tags/{tag_id}", response_model=review_tag_schemas.ReviewTagResponse)
async def delete_existing_review_tag(
    tag_id: int, 
    db: AsyncSession = Depends(get_db)
):
    # Delete a review tag by ID.
    deleted_tag = await crud_review_tag.delete_review_tag(db, review_tag_id=tag_id)
    if deleted_tag is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="ReviewTag not found")
    return deleted_tag

# New endpoints for CustomAIPrompt associated with a ReviewTag
@router.post("/review_tags/{tag_id}/custom_prompt/", response_model=custom_ai_prompt_schemas.CustomAIPromptResponse, status_code=status.HTTP_201_CREATED)
async def create_or_replace_tag_custom_ai_prompt(
    tag_id: int,
    prompt_in: custom_ai_prompt_schemas.CustomAIPromptBase, 
    db: AsyncSession = Depends(get_db)
):
    # Create or replace the custom AI prompt for a specific review tag.
    review_tag = await crud_review_tag.get_review_tag(db, review_tag_id=tag_id)
    if not review_tag:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="ReviewTag not found.")

    existing_prompt = await crud_custom_ai_prompt.get_custom_ai_prompt_by_review_tag_id(db, review_tag_id=tag_id)
    
    if existing_prompt:
        prompt_update_schema = custom_ai_prompt_schemas.CustomAIPromptUpdate(**prompt_in.dict(exclude_unset=True)) # Use exclude_unset for partial update
        updated_prompt = await crud_custom_ai_prompt.update_custom_ai_prompt(
            db, review_tag_id=tag_id, prompt_update=prompt_update_schema
        )
        return updated_prompt # Should not be None if existing_prompt was found
    else:
        prompt_create_schema = custom_ai_prompt_schemas.CustomAIPromptCreate(
            review_tag_id=tag_id, **prompt_in.dict()
        )
        try:
            return await crud_custom_ai_prompt.create_custom_ai_prompt(db, prompt_in=prompt_create_schema)
        except ValueError as e: # Catch specific errors from CRUD, e.g., if tag already has prompt (though logic here is upsert)
             raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


@router.get("/review_tags/{tag_id}/custom_prompt/", response_model=custom_ai_prompt_schemas.CustomAIPromptResponse)
async def read_tag_custom_ai_prompt(
    tag_id: int,
    db: AsyncSession = Depends(get_db)
):
    # Get the custom AI prompt for a specific review tag.
    prompt = await crud_custom_ai_prompt.get_custom_ai_prompt_by_review_tag_id(db, review_tag_id=tag_id)
    if prompt is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="CustomAIPrompt not found for this ReviewTag.")
    return prompt

@router.delete("/review_tags/{tag_id}/custom_prompt/", response_model=custom_ai_prompt_schemas.CustomAIPromptResponse)
async def delete_tag_custom_ai_prompt(
    tag_id: int,
    db: AsyncSession = Depends(get_db)
):
    # Delete the custom AI prompt for a specific review tag.
    review_tag = await crud_review_tag.get_review_tag(db, review_tag_id=tag_id) # Check parent tag exists
    if not review_tag:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="ReviewTag not found.")

    deleted_prompt = await crud_custom_ai_prompt.delete_custom_ai_prompt_by_review_tag_id(db, review_tag_id=tag_id)
    if deleted_prompt is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="CustomAIPrompt not found for this ReviewTag to delete.")
    return deleted_prompt
