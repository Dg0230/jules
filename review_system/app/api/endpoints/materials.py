from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query

from sqlalchemy.ext.asyncio import AsyncSession

from app.crud import crud_material
from app.schemas import material as material_schemas # Use alias for clarity
from app.models.material import MaterialStatusEnum # For query param validation
from app.core.db import get_db

router = APIRouter()

# API Endpoints for Materials

@router.post("/materials/", response_model=material_schemas.MaterialResponse, status_code=status.HTTP_201_CREATED)
async def create_new_material(
    material_in: material_schemas.MaterialCreate, 
    db: AsyncSession = Depends(get_db)
):
    return await crud_material.create_material(db=db, material=material_in)

@router.get("/materials/{material_id}", response_model=material_schemas.MaterialResponse)
async def read_material(
    material_id: int, 
    db: AsyncSession = Depends(get_db)
):
    db_material = await crud_material.get_material(db, material_id=material_id)
    if db_material is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Material not found")
    return db_material

@router.get("/materials/", response_model=List[material_schemas.MaterialResponse])
async def read_materials(
    skip: int = 0, 
    limit: int = 100, 
    status_filter: Optional[MaterialStatusEnum] = Query(None, alias="status"), # Use alias for query param
    merchant_id: Optional[int] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    materials = await crud_material.get_materials(
        db, skip=skip, limit=limit, status=status_filter, merchant_id=merchant_id
    )
    return materials

@router.put("/materials/{material_id}", response_model=material_schemas.MaterialResponse)
async def update_existing_material(
    material_id: int, 
    material_in: material_schemas.MaterialUpdate, 
    db: AsyncSession = Depends(get_db)
):
    db_material = await crud_material.update_material(db, material_id=material_id, material_update=material_in)
    if db_material is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Material not found")
    return db_material

@router.delete("/materials/{material_id}", response_model=material_schemas.MaterialResponse)
async def delete_existing_material(
    material_id: int, 
    db: AsyncSession = Depends(get_db)
):
    db_material = await crud_material.delete_material(db, material_id=material_id)
    if db_material is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Material not found")
    return db_material

# API Endpoints for MaterialSets

@router.post("/material_sets/", response_model=material_schemas.MaterialSetResponse, status_code=status.HTTP_201_CREATED)
async def create_new_material_set(
    material_set_in: material_schemas.MaterialSetCreate, 
    db: AsyncSession = Depends(get_db)
):
    # By default, MaterialSetResponse includes an empty list of materials.
    # The CRUD op creates a set without materials initially.
    return await crud_material.create_material_set(db=db, material_set=material_set_in)

@router.get("/material_sets/{set_id}", response_model=material_schemas.MaterialSetResponse)
async def read_material_set(
    set_id: int, 
    include_materials: bool = Query(False, description="Set to true to include materials in the response"),
    db: AsyncSession = Depends(get_db)
):
    db_material_set = await crud_material.get_material_set(db, material_set_id=set_id, include_materials=include_materials)
    if db_material_set is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="MaterialSet not found")
    return db_material_set

@router.get("/material_sets/", response_model=List[material_schemas.MaterialSetResponse])
async def read_material_sets(
    skip: int = 0, 
    limit: int = 100, 
    merchant_id: Optional[int] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    # Note: This currently doesn't include materials for each set in the list.
    # If needed, the CRUD function and schema would need adjustment for list views.
    material_sets = await crud_material.get_material_sets(db, skip=skip, limit=limit, merchant_id=merchant_id)
    return material_sets

@router.put("/material_sets/{set_id}", response_model=material_schemas.MaterialSetResponse)
async def update_existing_material_set(
    set_id: int, 
    material_set_in: material_schemas.MaterialSetUpdate, 
    db: AsyncSession = Depends(get_db)
):
    db_material_set = await crud_material.update_material_set(db, material_set_id=set_id, material_set_update=material_set_in)
    if db_material_set is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="MaterialSet not found")
    return db_material_set

@router.delete("/material_sets/{set_id}", response_model=material_schemas.MaterialSetResponse)
async def delete_existing_material_set(
    set_id: int, 
    db: AsyncSession = Depends(get_db)
):
    db_material_set = await crud_material.delete_material_set(db, material_set_id=set_id)
    if db_material_set is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="MaterialSet not found")
    return db_material_set

# API Endpoints for MaterialSet-Material Associations

@router.post("/material_sets/{set_id}/materials/{material_id}", response_model=material_schemas.MaterialSetResponse)
async def add_material_to_material_set(
    set_id: int, 
    material_id: int, 
    db: AsyncSession = Depends(get_db)
):
    db_material_set = await crud_material.add_material_to_set(db, material_set_id=set_id, material_id=material_id)
    if db_material_set is None: # Could mean set or material not found, or other issue
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="MaterialSet or Material not found, or association failed")
    return db_material_set

@router.delete("/material_sets/{set_id}/materials/{material_id}", response_model=material_schemas.MaterialSetResponse)
async def remove_material_from_material_set(
    set_id: int, 
    material_id: int, 
    db: AsyncSession = Depends(get_db)
):
    db_material_set = await crud_material.remove_material_from_set(db, material_set_id=set_id, material_id=material_id)
    if db_material_set is None: # Could mean set or material not found
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="MaterialSet or Material not found, or disassociation failed")
    return db_material_set
