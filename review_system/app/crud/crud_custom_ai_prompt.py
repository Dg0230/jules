from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

from app.models.custom_ai_prompt import CustomAIPrompt
from app.models.review_tag import ReviewTag 
from app.schemas.custom_ai_prompt import CustomAIPromptCreate, CustomAIPromptUpdate

async def create_custom_ai_prompt(
    db: AsyncSession, 
    prompt_in: CustomAIPromptCreate
) -> CustomAIPrompt:
    # Create a new custom AI prompt.
    review_tag = await db.get(ReviewTag, prompt_in.review_tag_id)
    if not review_tag:
        raise ValueError(f"ReviewTag with id {prompt_in.review_tag_id} not found.")

    existing_prompt = await get_custom_ai_prompt_by_review_tag_id(db, review_tag_id=prompt_in.review_tag_id)
    if existing_prompt:
        raise ValueError(f"CustomAIPrompt already exists for ReviewTag id {prompt_in.review_tag_id}. Update it instead.")

    db_prompt = CustomAIPrompt(**prompt_in.dict())
    db.add(db_prompt)
    await db.commit()
    await db.refresh(db_prompt)
    return db_prompt

async def get_custom_ai_prompt(db: AsyncSession, prompt_id: int) -> Optional[CustomAIPrompt]:
    # Get a custom AI prompt by its ID.
    result = await db.execute(select(CustomAIPrompt).filter(CustomAIPrompt.id == prompt_id))
    return result.scalars().first()

async def get_custom_ai_prompt_by_review_tag_id(
    db: AsyncSession, 
    review_tag_id: int
) -> Optional[CustomAIPrompt]:
    # Get a custom AI prompt by review_tag_id.
    result = await db.execute(select(CustomAIPrompt).filter(CustomAIPrompt.review_tag_id == review_tag_id))
    return result.scalars().first()

async def update_custom_ai_prompt(
    db: AsyncSession, 
    review_tag_id: int, 
    prompt_update: CustomAIPromptUpdate
) -> Optional[CustomAIPrompt]:
    # Update a custom AI prompt.
    db_prompt = await get_custom_ai_prompt_by_review_tag_id(db, review_tag_id=review_tag_id)
    if db_prompt is None:
        return None 
    
    update_data = prompt_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_prompt, key, value)
        
    await db.commit()
    await db.refresh(db_prompt)
    return db_prompt

async def delete_custom_ai_prompt_by_review_tag_id(
    db: AsyncSession, 
    review_tag_id: int
) -> Optional[CustomAIPrompt]:
    # Delete a custom AI prompt by review_tag_id.
    db_prompt = await get_custom_ai_prompt_by_review_tag_id(db, review_tag_id=review_tag_id)
    if db_prompt is None:
        return None
    
    await db.delete(db_prompt)
    await db.commit()
    return db_prompt
