from sqlalchemy import Column, Integer, String, Text
from sqlalchemy.orm import relationship

from app.models.base import Base

class Merchant(Base):
    __tablename__ = "merchants"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), index=True, nullable=False)
    contact_info = Column(Text, nullable=True) 

    materials = relationship("Material", back_populates="merchant", cascade="all, delete-orphan")
    material_sets = relationship("MaterialSet", back_populates="merchant", cascade="all, delete-orphan")
    
    # Add relationship to MerchantFinancials (One-to-One)
    financials = relationship("MerchantFinancials", back_populates="merchant", uselist=False, cascade="all, delete-orphan")
    
    # reviews = relationship("Review", back_populates="merchant", cascade="all, delete-orphan") # Placeholder

    def __repr__(self):
        return f"<Merchant(id={self.id}, name='{self.name}')>"
