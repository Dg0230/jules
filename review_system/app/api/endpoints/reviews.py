from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Path, Query

from sqlalchemy.ext.asyncio import AsyncSession

from app.crud import crud_review 
from app.schemas import review as review_schemas 
from app.models.review import ReviewPlatformEnum 
from app.models.merchant import Merchant 
from app.core.db import get_db

router = APIRouter()

@router.post("/", response_model=review_schemas.ReviewResponse, status_code=status.HTTP_201_CREATED)
async def submit_new_review(
    review_in: review_schemas.ReviewCreate,
    db: AsyncSession = Depends(get_db)
):
    # Submit a new review for a merchant.
    merchant = await db.get(Merchant, review_in.merchant_id)
    if not merchant:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Merchant with id {review_in.merchant_id} not found.")
    
    try:
        return await crud_review.create_review(db=db, review_in=review_in)
    except ValueError as e: 
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


@router.get("/merchants/{merchant_id}/timeline/", response_model=List[review_schemas.ReviewResponse])
async def read_merchant_review_timeline(
    merchant_id: int = Path(..., description="ID of the merchant to retrieve the review timeline for"),
    review_tag_id: Optional[int] = Query(None, description="Filter timeline by a specific ReviewTag ID"),
    platform: Optional[ReviewPlatformEnum] = Query(None, description="Filter by review platform"),
    is_verified: Optional[bool] = Query(None, description="Filter by verification status"),
    is_positive: Optional[bool] = Query(None, description="Filter by positive status"),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=200), 
    db: AsyncSession = Depends(get_db)
):
    # Retrieve the review timeline for a specific merchant.
    merchant = await db.get(Merchant, merchant_id)
    if not merchant:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Merchant with id {merchant_id} not found.")

    reviews = await crud_review.get_reviews_by_merchant(
        db, 
        merchant_id=merchant_id, 
        skip=skip, 
        limit=limit,
        platform=platform,
        is_verified=is_verified,
        is_positive=is_positive,
        review_tag_id=review_tag_id 
    )
    return reviews
