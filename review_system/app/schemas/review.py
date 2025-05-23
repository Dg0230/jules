from typing import Optional
from pydantic import BaseModel, HttpUrl
from datetime import datetime
from app.models.review import ReviewPlatformEnum # Import Enum

class ReviewBase(BaseModel):
    merchant_id: int
    platform: ReviewPlatformEnum
    is_positive: Optional[bool] = True
    screenshot_url: Optional[HttpUrl] = None # Validate as URL
    is_verified: Optional[bool] = False

class ReviewCreate(ReviewBase):
    pass # merchant_id will be part of the path or payload

class ReviewUpdate(BaseModel): # Specific update schema
    is_positive: Optional[bool] = None
    screenshot_url: Optional[HttpUrl] = None
    is_verified: Optional[bool] = None
    # Platform and merchant_id are generally not updatable for a review

class ReviewResponse(ReviewBase):
    id: int
    review_date: datetime
    # Potentially include merchant details or material details if needed in responses

    class Config:
        orm_mode = True
