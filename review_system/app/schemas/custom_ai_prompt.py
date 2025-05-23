from typing import Optional, Dict, Any
from pydantic import BaseModel

class CustomAIPromptBase(BaseModel):
    prompt_text: Optional[str] = None
    style_config: Optional[Dict[str, Any]] = None # JSON field will be dict in Pydantic

class CustomAIPromptCreate(CustomAIPromptBase):
    review_tag_id: int # Must be provided on creation

class CustomAIPromptUpdate(CustomAIPromptBase):
    # All fields are optional for update
    pass

class CustomAIPromptResponse(CustomAIPromptBase):
    id: int
    review_tag_id: int

    class Config:
        orm_mode = True
