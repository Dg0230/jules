from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

from app.models.prompt_template import PromptTemplate, PromptTemplateCategoryEnum
from app.schemas.prompt_template import PromptTemplateCreate, PromptTemplateUpdate

async def create_prompt_template(db: AsyncSession, template_in: PromptTemplateCreate) -> PromptTemplate:
    # Create a new prompt template.
    db_template = PromptTemplate(**template_in.dict())
    db.add(db_template)
    await db.commit()
    await db.refresh(db_template)
    return db_template

async def get_prompt_template(db: AsyncSession, template_id: int) -> Optional[PromptTemplate]:
    # Get a single prompt template by ID.
    result = await db.execute(select(PromptTemplate).filter(PromptTemplate.id == template_id))
    return result.scalars().first()

async def get_prompt_templates(
    db: AsyncSession, 
    skip: int = 0, 
    limit: int = 100,
    category: Optional[PromptTemplateCategoryEnum] = None
) -> List[PromptTemplate]:
    # Get a list of prompt templates with pagination and optional category filter.
    query = select(PromptTemplate)
    if category:
        query = query.filter(PromptTemplate.category == category)
    query = query.order_by(PromptTemplate.name).offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()

async def update_prompt_template(
    db: AsyncSession, 
    template_id: int, 
    template_update: PromptTemplateUpdate
) -> Optional[PromptTemplate]:
    # Update an existing prompt template.
    db_template = await get_prompt_template(db, template_id)
    if db_template is None:
        return None
    
    update_data = template_update.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_template, key, value)
        
    await db.commit()
    await db.refresh(db_template)
    return db_template

async def delete_prompt_template(db: AsyncSession, template_id: int) -> Optional[PromptTemplate]:
    # Delete a prompt template by ID.
    db_template = await get_prompt_template(db, template_id)
    if db_template is None:
        return None
    
    await db.delete(db_template)
    await db.commit()
    return db_template
