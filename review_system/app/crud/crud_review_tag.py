from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy.orm import selectinload

from app.models.review_tag import ReviewTag
from app.models.material import MaterialSet 
from app.models.merchant import Merchant 
from app.schemas.review_tag import ReviewTagCreate, ReviewTagUpdate

async def create_review_tag(db: AsyncSession, review_tag_in: ReviewTagCreate) -> ReviewTag:
    # Create a new review tag and associate it with specified material sets.
    merchant = await db.get(Merchant, review_tag_in.merchant_id)
    if not merchant:
        raise ValueError(f"Merchant with id {review_tag_in.merchant_id} not found.")

    db_review_tag = ReviewTag(
        name=review_tag_in.name,
        merchant_id=review_tag_in.merchant_id,
        status=review_tag_in.status 
    )

    if review_tag_in.material_set_ids:
        query = select(MaterialSet).filter(MaterialSet.id.in_(review_tag_in.material_set_ids))
        result = await db.execute(query)
        material_sets_to_associate = result.scalars().all()
        
        valid_material_sets = []
        for ms in material_sets_to_associate:
            if ms.merchant_id == review_tag_in.merchant_id:
                valid_material_sets.append(ms)
            else:
                raise ValueError(f"MaterialSet id {ms.id} does not belong to merchant {review_tag_in.merchant_id}.")

        if len(valid_material_sets) != len(set(review_tag_in.material_set_ids)):
             raise ValueError("One or more MaterialSet IDs are invalid or not found.")
        
        db_review_tag.material_sets = valid_material_sets

    db.add(db_review_tag)
    await db.commit()
    await db.refresh(db_review_tag)
    # Eager load material_sets after creation/refresh for the return object
    # This ensures the returned object has the relationships populated as expected by some schemas/callers.
    # A separate refresh with attribute_names is one way to do this.
    await db.refresh(db_review_tag, attribute_names=['material_sets']) 
    return db_review_tag

async def get_review_tag(db: AsyncSession, review_tag_id: int) -> Optional[ReviewTag]:
    # Get a single review tag by ID, including its associated material sets.
    result = await db.execute(
        select(ReviewTag)
        .options(selectinload(ReviewTag.material_sets)) 
        .filter(ReviewTag.id == review_tag_id)
    )
    return result.scalars().first()

async def get_review_tags_by_merchant(
    db: AsyncSession, 
    merchant_id: int,
    skip: int = 0, 
    limit: int = 100
) -> List[ReviewTag]:
    # Get a list of review tags for a specific merchant with pagination.
    # Includes associated material sets.
    query = (
        select(ReviewTag)
        .options(selectinload(ReviewTag.material_sets)) 
        .filter(ReviewTag.merchant_id == merchant_id)
        .order_by(ReviewTag.name) 
        .offset(skip)
        .limit(limit)
    )
    result = await db.execute(query)
    return result.scalars().all()

async def update_review_tag(
    db: AsyncSession, 
    review_tag_id: int, 
    review_tag_update: ReviewTagUpdate
) -> Optional[ReviewTag]:
    # Update an existing review tag. Allows updating name, status, and material set associations.
    db_review_tag = await get_review_tag(db, review_tag_id) 
    if db_review_tag is None:
        return None
    
    update_data = review_tag_update.dict(exclude_unset=True)

    if "name" in update_data:
        db_review_tag.name = update_data["name"]
    if "status" in update_data:
        db_review_tag.status = update_data["status"]

    # Handle material_set_ids update explicitly.
    # If material_set_ids is present in the payload (even if empty list), update associations.
    # If material_set_ids is not in the payload (None), associations are not touched.
    if review_tag_update.material_set_ids is not None: 
        if not review_tag_update.material_set_ids: # Empty list means disassociate all
            db_review_tag.material_sets = []
        else:
            query = select(MaterialSet).filter(MaterialSet.id.in_(review_tag_update.material_set_ids))
            result = await db.execute(query)
            material_sets_to_associate = result.scalars().all()

            valid_material_sets = []
            for ms in material_sets_to_associate:
                # Ensure MaterialSets belong to the same merchant as the ReviewTag
                if ms.merchant_id == db_review_tag.merchant_id: 
                    valid_material_sets.append(ms)
                else:
                    # Important: Raise error if a MaterialSet does not belong to the tag's merchant
                    raise ValueError(f"MaterialSet id {ms.id} does not belong to merchant {db_review_tag.merchant_id}.")
            
            # Ensure all specified IDs were found and valid
            if len(valid_material_sets) != len(set(review_tag_update.material_set_ids)):
                 raise ValueError("One or more MaterialSet IDs for update are invalid or not found.")

            db_review_tag.material_sets = valid_material_sets
        
    await db.commit()
    await db.refresh(db_review_tag)
    # Eager load material_sets after update/refresh for the return object
    await db.refresh(db_review_tag, attribute_names=['material_sets'])
    return db_review_tag

async def delete_review_tag(db: AsyncSession, review_tag_id: int) -> Optional[ReviewTag]:
    # Delete a review tag by ID.
    # The M2M associations in reviewtag_materialset_association will be handled by SQLAlchemy automatically
    # if the relationship is configured with cascade="all, delete" or similar on the association object,
    # or if they are manually cleared. Here, we rely on default SQLAlchemy behavior for M2M which
    # should just remove entries from the association table.
    db_review_tag = await get_review_tag(db, review_tag_id) # This already loads material_sets
    if db_review_tag is None:
        return None
    
    await db.delete(db_review_tag)
    await db.commit()
    # The returned object will still have its relationships loaded from before deletion.
    return db_review_tag
