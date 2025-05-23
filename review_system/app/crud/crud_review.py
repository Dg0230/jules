from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import func # For count aggregations

from app.models.review import Review, ReviewPlatformEnum
from app.models.merchant import Merchant # To check merchant existence
from app.schemas.review import ReviewCreate, ReviewUpdate

async def create_review(db: AsyncSession, review_in: ReviewCreate) -> Review:
    # Create a new review.
    merchant = await db.get(Merchant, review_in.merchant_id)
    if not merchant:
        raise ValueError(f"Merchant with id {review_in.merchant_id} not found.")

    db_review = Review(**review_in.dict())
    db.add(db_review)
    await db.commit()
    await db.refresh(db_review)
    return db_review

async def get_review(db: AsyncSession, review_id: int) -> Optional[Review]:
    # Get a single review by ID.
    result = await db.execute(select(Review).filter(Review.id == review_id))
    return result.scalars().first()

async def get_reviews_by_merchant(
    db: AsyncSession, 
    merchant_id: int,
    skip: int = 0, 
    limit: int = 100,
    platform: Optional[ReviewPlatformEnum] = None,
    is_verified: Optional[bool] = None,
    is_positive: Optional[bool] = None,
    review_tag_id: Optional[int] = None # Add this new parameter
) -> List[Review]:
    # Get a list of reviews for a specific merchant with pagination and optional filters.
    query = select(Review).filter(Review.merchant_id == merchant_id)
    if platform:
        query = query.filter(Review.platform == platform)
    if is_verified is not None:
        query = query.filter(Review.is_verified == is_verified)
    if is_positive is not None:
        query = query.filter(Review.is_positive == is_positive)
    if review_tag_id is not None: # Add this filter condition
        query = query.filter(Review.review_tag_id == review_tag_id)
        
    query = query.order_by(Review.review_date.desc()).offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()
    
async def get_reviews_count_by_merchant(
    db: AsyncSession,
    merchant_id: int,
    is_positive: Optional[bool] = None,
    is_verified: Optional[bool] = None
) -> int:
    # Get the count of reviews for a specific merchant, with optional filters.
    # This is a helper for dashboard data.
    query = select(func.count(Review.id)).filter(Review.merchant_id == merchant_id)
    if is_positive is not None:
        query = query.filter(Review.is_positive == is_positive)
    if is_verified is not None:
        query = query.filter(Review.is_verified == is_verified)
    
    result = await db.execute(query)
    count = result.scalar_one_or_none()
    return count if count is not None else 0

async def update_review(
    db: AsyncSession, 
    review_id: int, 
    review_update: ReviewUpdate
) -> Optional[Review]:
    # Update an existing review.
    # Typically used for verification or minor corrections.
    db_review = await get_review(db, review_id)
    if db_review is None:
        return None
    
    update_data = review_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_review, key, value)
        
    await db.commit()
    await db.refresh(db_review)
    return db_review

async def delete_review(db: AsyncSession, review_id: int) -> Optional[Review]:
    # Delete a review by ID.
    db_review = await get_review(db, review_id)
    if db_review is None:
        return None
    
    await db.delete(db_review)
    await db.commit()
    return db_review
