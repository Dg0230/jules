from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Path, Query

from sqlalchemy.ext.asyncio import AsyncSession

from app.crud import crud_prompt_template
from app.schemas import prompt_template as prompt_template_schemas 
from app.models.prompt_template import PromptTemplateCategoryEnum 
from app.core.db import get_db

router = APIRouter()

@router.get("/", response_model=List[prompt_template_schemas.PromptTemplateResponse])
async def read_all_prompt_templates(
    skip: int = 0,
    limit: int = 100,
    category: Optional[PromptTemplateCategoryEnum] = Query(None, description="Filter templates by category"),
    db: AsyncSession = Depends(get_db)
):
    # List all available prompt templates for merchants to browse.
    # Supports pagination and filtering by category.
    templates = await crud_prompt_template.get_prompt_templates(
        db, skip=skip, limit=limit, category=category
    )
    return templates

@router.get("/{template_id}", response_model=prompt_template_schemas.PromptTemplateResponse)
async def read_prompt_template_by_id(
    template_id: int = Path(..., title="The ID of the prompt template to retrieve"),
    db: AsyncSession = Depends(get_db)
):
    # Get details of a specific prompt template.
    db_template = await crud_prompt_template.get_prompt_template(db, template_id=template_id)
    if db_template is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="PromptTemplate not found")
    return db_template
