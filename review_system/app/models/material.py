import enum
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Table, Enum as SQLAlchemyEnum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func # For server-side default timestamp
from app.models.base import Base # Import Base from the new file

# Define an association table for the many-to-many relationship
# between Material and MaterialSet.
material_materialset_association = Table(
    'material_materialset_association', Base.metadata,
    Column('material_id', Integer, ForeignKey('materials.id'), primary_key=True),
    Column('materialset_id', Integer, ForeignKey('materialsets.id'), primary_key=True)
)

class FileTypeEnum(enum.Enum):
    IMAGE = "image"
    VIDEO = "video"
    TEXT = "text"

class MaterialStatusEnum(enum.Enum):
    UNUSED = "unused"
    USED = "used"
    EXPIRED = "expired"

class Material(Base):
    __tablename__ = "materials"

    id = Column(Integer, primary_key=True, index=True)
    # In a multi-tenant app, merchant_id would link to a Merchants table.
    # For now, we can make it an Integer or String, assuming merchant identity is handled elsewhere or will be added later.
    merchant_id = Column(Integer, index=True, nullable=False) # Assuming a simple integer ID for now
    
    content_url = Column(String, nullable=False) # URL or path to the material content
    file_type = Column(SQLAlchemyEnum(FileTypeEnum), nullable=False)
    
    upload_date = Column(DateTime(timezone=True), server_default=func.now())
    status = Column(SQLAlchemyEnum(MaterialStatusEnum), nullable=False, default=MaterialStatusEnum.UNUSED)
    
    # Using sqlalchemy.dialects.postgresql.ARRAY for tags if specific to PostgreSQL and want array type
    # For more general JSONB is also an option: from sqlalchemy.dialects.postgresql import JSONB
    # tags = Column(JSONB) 
    # For simplicity, let's use a string for tags, assuming comma-separated values or similar.
    # This can be changed to a more complex type like ARRAY or JSONB if advanced querying on tags is needed.
    tags = Column(String, nullable=True) 

    # Relationship to MaterialSet (many-to-many)
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
    merchant_id = Column(Integer, index=True, nullable=False) # Link to the merchant who owns this set
    name = Column(String, index=True, nullable=False)
    description = Column(String, nullable=True)

    # Relationship to Material (many-to-many)
    materials = relationship(
        "Material",
        secondary=material_materialset_association,
        back_populates="sets"
    )

    def __repr__(self):
        return f"<MaterialSet(id={self.id}, name='{self.name}', merchant_id={self.merchant_id})>"
