from sqlalchemy import Column, Integer, String, Text, ForeignKey # Ensure ForeignKey is imported
from sqlalchemy.orm import relationship # Ensure relationship is imported

from app.models.base import Base
# from .channel_partner import ChannelPartner # Not needed for string reference

class Merchant(Base):
    __tablename__ = "merchants"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), index=True, nullable=False)
    contact_info = Column(Text, nullable=True) 

    # New field: Link to the ChannelPartner that may have referred/manages this merchant
    channel_partner_id = Column(Integer, ForeignKey("channel_partners.id"), nullable=True, index=True)

    # Relationships
    # Existing relationships to Material, MaterialSet, MerchantFinancials, Review should be preserved
    materials = relationship("Material", back_populates="merchant", cascade="all, delete-orphan")
    material_sets = relationship("MaterialSet", back_populates="merchant", cascade="all, delete-orphan")
    financials = relationship("MerchantFinancials", back_populates="merchant", uselist=False, cascade="all, delete-orphan")
    reviews = relationship("Review", back_populates="merchant", cascade="all, delete-orphan")
    review_tags = relationship("ReviewTag", back_populates="merchant", cascade="all, delete-orphan") # This was added previously

    # New relationship to ChannelPartner
    channel_partner = relationship("ChannelPartner", back_populates="merchants")

    def __repr__(self):
        return f"<Merchant(id={self.id}, name='{self.name}')>"
