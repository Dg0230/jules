from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy.orm import selectinload # For eager loading relationships

from app.models.material import Material, MaterialSet, MaterialStatusEnum
from app.schemas.material import MaterialCreate, MaterialUpdate, MaterialSetCreate, MaterialSetUpdate

# CRUD for Material
async def create_material(db: AsyncSession, material: MaterialCreate) -> Material:
    db_material = Material(**material.dict())
    db.add(db_material)
    await db.commit()
    await db.refresh(db_material)
    return db_material

async def get_material(db: AsyncSession, material_id: int) -> Optional[Material]:
    result = await db.execute(select(Material).filter(Material.id == material_id))
    return result.scalars().first()

async def get_materials(
    db: AsyncSession, 
    skip: int = 0, 
    limit: int = 100, 
    status: Optional[MaterialStatusEnum] = None,
    merchant_id: Optional[int] = None
) -> List[Material]:
    query = select(Material)
    if status:
        query = query.filter(Material.status == status)
    if merchant_id:
        query = query.filter(Material.merchant_id == merchant_id)
    query = query.offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()

async def update_material(db: AsyncSession, material_id: int, material_update: MaterialUpdate) -> Optional[Material]:
    db_material = await get_material(db, material_id)
    if db_material is None:
        return None
    
    update_data = material_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_material, key, value)
        
    await db.commit()
    await db.refresh(db_material)
    return db_material

async def delete_material(db: AsyncSession, material_id: int) -> Optional[Material]:
    db_material = await get_material(db, material_id)
    if db_material is None:
        return None
    await db.delete(db_material)
    await db.commit()
    return db_material

# CRUD for MaterialSet
async def create_material_set(db: AsyncSession, material_set: MaterialSetCreate) -> MaterialSet:
    db_material_set = MaterialSet(**material_set.dict())
    db.add(db_material_set)
    await db.commit()
    await db.refresh(db_material_set)
    return db_material_set

async def get_material_set(db: AsyncSession, material_set_id: int, include_materials: bool = False) -> Optional[MaterialSet]:
    query = select(MaterialSet).filter(MaterialSet.id == material_set_id)
    if include_materials:
        query = query.options(selectinload(MaterialSet.materials)) # Eager load materials
    result = await db.execute(query)
    return result.scalars().first()

async def get_material_sets(
    db: AsyncSession, 
    skip: int = 0, 
    limit: int = 100,
    merchant_id: Optional[int] = None
) -> List[MaterialSet]:
    query = select(MaterialSet)
    if merchant_id:
        query = query.filter(MaterialSet.merchant_id == merchant_id)
    query = query.offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()
    
async def update_material_set(db: AsyncSession, material_set_id: int, material_set_update: MaterialSetUpdate) -> Optional[MaterialSet]:
    db_material_set = await get_material_set(db, material_set_id)
    if db_material_set is None:
        return None
    
    update_data = material_set_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_material_set, key, value)
        
    await db.commit()
    await db.refresh(db_material_set)
    return db_material_set

async def delete_material_set(db: AsyncSession, material_set_id: int) -> Optional[MaterialSet]:
    db_material_set = await get_material_set(db, material_set_id)
    if db_material_set is None:
        return None
    await db.delete(db_material_set)
    await db.commit()
    return db_material_set

# CRUD for MaterialSet and Material association
async def add_material_to_set(db: AsyncSession, material_set_id: int, material_id: int) -> Optional[MaterialSet]:
    db_material_set = await get_material_set(db, material_set_id, include_materials=True)
    if not db_material_set:
        return None
    
    db_material = await get_material(db, material_id)
    if not db_material:
        return None # Or raise an exception

    if db_material not in db_material_set.materials:
        db_material_set.materials.append(db_material)
        await db.commit()
        await db.refresh(db_material_set)
    return db_material_set

async def remove_material_from_set(db: AsyncSession, material_set_id: int, material_id: int) -> Optional[MaterialSet]:
    db_material_set = await get_material_set(db, material_set_id, include_materials=True)
    if not db_material_set:
        return None

    db_material = await get_material(db, material_id)
    if not db_material:
        return None # Or raise an exception

    if db_material in db_material_set.materials:
        db_material_set.materials.remove(db_material)
        await db.commit()
        await db.refresh(db_material_set)
    return db_material_set
