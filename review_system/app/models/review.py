import enum
from sqlalchemy import Column, Integer, String, DateTime, Boolean, ForeignKey, Enum as SQLAlchemyEnum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func 

from app.models.base import Base
# ReviewPlatformEnum should already be here
# from .review_tag import ReviewTag # Not needed for string reference in relationship

class ReviewPlatformEnum(enum.Enum):
    MEITUAN = "meituan"
    CTRIP = "ctrip"
    DAZHONG_DIANPING = "dazhong_dianping"
    XIAOHONGSHU = "xiaohongshu"
    OTHER = "other"

class Review(Base):
    __tablename__ = "reviews"

    id = Column(Integer, primary_key=True, index=True)
    merchant_id = Column(Integer, ForeignKey("merchants.id"), nullable=False, index=True)
    
    platform = Column(SQLAlchemyEnum(ReviewPlatformEnum), nullable=False)
    review_date = Column(DateTime(timezone=True), server_default=func.now())
    is_positive = Column(Boolean, default=True) 
    screenshot_url = Column(String, nullable=True) 
    is_verified = Column(Boolean, default=False)

    # New field: Link to the ReviewTag that might have generated this review
    review_tag_id = Column(Integer, ForeignKey("review_tags.id"), nullable=True, index=True)

    # Relationships
    merchant = relationship("Merchant", back_populates="reviews")
    review_tag = relationship("ReviewTag", back_populates="reviews") # New relationship

    def __repr__(self):
        return f"<Review(id={self.id}, merchant_id={self.merchant_id}, platform='{self.platform.value}')>"
