import enum
from sqlalchemy import Column, Integer, String, DateTime, Boolean, ForeignKey, Enum as SQLAlchemyEnum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func # For server-side default timestamp

from app.models.base import Base

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
    
    # customer_id: Link to a customer table if/when it exists. For now, can be nullable string or omitted.
    # associated_material_id: Link to material used for review if applicable.
    
    platform = Column(SQLAlchemyEnum(ReviewPlatformEnum), nullable=False)
    review_date = Column(DateTime(timezone=True), server_default=func.now())
    is_positive = Column(Boolean, default=True) # Assuming most will be positive as per system goal
    
    # For "上传分享成功凭证 (用于兑换奖励)" and "评价截图核销数量"
    screenshot_url = Column(String, nullable=True) 
    is_verified = Column(Boolean, default=False) # Verified by merchant/system

    # Relationship to Merchant
    merchant = relationship("Merchant", back_populates="reviews")
    
    # Optional: Link to Material used for this review
    # material_id = Column(Integer, ForeignKey("materials.id"), nullable=True)
    # material = relationship("Material") # Define back_populates on Material if needed

    def __repr__(self):
        return f"<Review(id={self.id}, merchant_id={self.merchant_id}, platform='{self.platform.value}')>"
