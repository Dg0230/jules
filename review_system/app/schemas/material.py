from typing import List, Optional
from pydantic import BaseModel, HttpUrl
from datetime import datetime
from app.models.material import FileTypeEnum, MaterialStatusEnum # Import Enums from models

# Base schema for Material, containing common attributes
class MaterialBase(BaseModel):
    merchant_id: int
    content_url: HttpUrl # Validate as URL
    file_type: FileTypeEnum
    tags: Optional[str] = None

# Schema for creating a Material (request body)
class MaterialCreate(MaterialBase):
    pass

# Schema for updating a Material (request body)
# All fields are optional for partial updates
class MaterialUpdate(BaseModel):
    content_url: Optional[HttpUrl] = None
    file_type: Optional[FileTypeEnum] = None
    status: Optional[MaterialStatusEnum] = None
    tags: Optional[str] = None

# Schema for representing a Material in responses
# This includes fields that are auto-generated or managed by the server
class MaterialResponse(MaterialBase):
    id: int
    upload_date: datetime
    status: MaterialStatusEnum

    class Config:
        orm_mode = True # Enable ORM mode to work with SQLAlchemy models

# Base schema for MaterialSet
class MaterialSetBase(BaseModel):
    merchant_id: int
    name: str
    description: Optional[str] = None

# Schema for creating a MaterialSet (request body)
class MaterialSetCreate(MaterialSetBase):
    pass

# Schema for updating a MaterialSet (request body)
class MaterialSetUpdate(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None

# Schema for representing a MaterialSet in responses, potentially including its materials
class MaterialSetResponse(MaterialSetBase):
    id: int
    # Optionally, include materials in the response.
    # This can be controlled further, e.g., by a separate endpoint or query parameter.
    materials: List[MaterialResponse] = []


    class Config:
        orm_mode = True

# Schema for adding/removing material to/from a set
class MaterialSetAssociation(BaseModel):
    material_id: int
    materialset_id: int
