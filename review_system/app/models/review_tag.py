import enum
import uuid
from sqlalchemy import Column, Integer, String, Enum as SQLAlchemyEnum, ForeignKey, UniqueConstraint, desc # Import desc
from sqlalchemy.orm import relationship 
from sqlalchemy.dialects.postgresql import UUID

from app.models.base import Base
from app.models.associations import reviewtag_materialset_association
# from app.models.review import Review # Not strictly needed for string reference "Review" but good for clarity

class ReviewTagStatusEnum(enum.Enum):
    ACTIVE = "active"
    INACTIVE = "inactive"

class ReviewTag(Base):
    __tablename__ = "review_tags"

    id = Column(Integer, primary_key=True, index=True)
    merchant_id = Column(Integer, ForeignKey("merchants.id"), nullable=False, index=True)
    
    name = Column(String(255), nullable=False, comment="User-friendly name for the tag, e.g., 'Table 5 QR Code'")
    tag_identifier = Column(UUID(as_uuid=True), default=uuid.uuid4, nullable=False, unique=True, index=True)
    status = Column(SQLAlchemyEnum(ReviewTagStatusEnum), nullable=False, default=ReviewTagStatusEnum.ACTIVE)

    merchant = relationship("Merchant", back_populates="review_tags")
    material_sets = relationship(
        "MaterialSet",
        secondary=reviewtag_materialset_association,
        back_populates="review_tags"
    )
    
    custom_ai_prompt = relationship(
        "CustomAIPrompt", 
        back_populates="review_tag", 
        uselist=False, 
        cascade="all, delete-orphan"
    )

    # New relationship: One ReviewTag can lead to many Reviews
    # Using "Review" as a string to avoid circular import issues if Review also imports ReviewTag
    reviews = relationship("Review", back_populates="review_tag", order_by="desc(Review.review_date)")


    __table_args__ = (UniqueConstraint('merchant_id', 'name', name='_merchant_tag_name_uc'),)

    def __repr__(self):
        return f"<ReviewTag(id={self.id}, name='{self.name}', merchant_id={self.merchant_id})>"
