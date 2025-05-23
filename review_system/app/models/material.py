import enum
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Table, Enum as SQLAlchemyEnum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func # For server-side default timestamp
from app.models.base import Base
# Removed: from app.models.merchant import Merchant

class FileTypeEnum(enum.Enum):
    IMAGE = "image"
    VIDEO = "video"
    TEXT = "text"

class MaterialStatusEnum(enum.Enum):
    UNUSED = "unused"
    USED = "used"
    EXPIRED = "expired"

material_materialset_association = Table(
    'material_materialset_association', Base.metadata,
    Column('material_id', Integer, ForeignKey('materials.id'), primary_key=True),
    Column('materialset_id', Integer, ForeignKey('materialsets.id'), primary_key=True)
)

class Material(Base):
    __tablename__ = "materials"

    id = Column(Integer, primary_key=True, index=True)
    
    # Updated merchant_id to be a ForeignKey
    merchant_id = Column(Integer, ForeignKey("merchants.id"), nullable=False, index=True)
    
    content_url = Column(String, nullable=False)
    file_type = Column(SQLAlchemyEnum(FileTypeEnum), nullable=False)
    upload_date = Column(DateTime(timezone=True), server_default=func.now())
    status = Column(SQLAlchemyEnum(MaterialStatusEnum), nullable=False, default=MaterialStatusEnum.UNUSED)
    tags = Column(String, nullable=True) 

    # Define relationship to Merchant
    merchant = relationship("Merchant", back_populates="materials")

    sets = relationship(
        "MaterialSet",
        secondary=material_materialset_association,
        back_populates="materials"
    )

    def __repr__(self):
        return f"<Material(id={self.id}, merchant_id={self.merchant_id}, type='{self.file_type.value}')>"


class MaterialSet(Base):
    __tablename__ = "materialsets"

    id = Column(Integer, primary_key=True, index=True)
    
    # Updated merchant_id to be a ForeignKey
    merchant_id = Column(Integer, ForeignKey("merchants.id"), nullable=False, index=True)
    
    name = Column(String, index=True, nullable=False)
    description = Column(String, nullable=True)

    # Define relationship to Merchant
    merchant = relationship("Merchant", back_populates="material_sets")
    
    materials = relationship(
        "Material",
        secondary=material_materialset_association,
        back_populates="sets"
    )

    def __repr__(self):
        return f"<MaterialSet(id={self.id}, name='{self.name}', merchant_id={self.merchant_id})>"
