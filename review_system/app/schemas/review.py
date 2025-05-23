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
    review_tag_id: Optional[int] = None # Add this

class ReviewCreate(ReviewBase):
    pass # merchant_id will be part of the path or payload

class ReviewUpdate(BaseModel): # Specific update schema
    is_positive: Optional[bool] = None
    screenshot_url: Optional[HttpUrl] = None
    is_verified: Optional[bool] = None
    # review_tag_id is usually set at creation, not typically updated.
    # If it needs to be updatable, add it here.
    review_tag_id: Optional[int] = None # Allow updating if needed

class ReviewResponse(ReviewBase): # Inherits review_tag_id from ReviewBase
    id: int
    review_date: datetime
    # Potentially include merchant details or material details if needed in responses
    # review_tag: Optional[ReviewTagResponse] = None # If you want to nest the tag details

    class Config:
        orm_mode = True
