from typing import Optional
from pydantic import BaseModel
from app.models.prompt_template import PromptTemplateCategoryEnum

class PromptTemplateBase(BaseModel):
    name: str
    category: PromptTemplateCategoryEnum
    text: str

class PromptTemplateCreate(PromptTemplateBase):
    # For admin use primarily
    pass

class PromptTemplateUpdate(BaseModel):
    # For admin use primarily
    name: Optional[str] = None
    category: Optional[PromptTemplateCategoryEnum] = None
    text: Optional[str] = None

class PromptTemplateResponse(PromptTemplateBase):
    id: int

    class Config:
        orm_mode = True
