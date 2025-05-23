import enum
from sqlalchemy import Column, Integer, String, Text, Enum as SQLAlchemyEnum

from app.models.base import Base

class PromptTemplateCategoryEnum(enum.Enum):
    HOLIDAY = "holiday"
    SEASONAL = "seasonal"
    EVENT = "event" # Special events
    GENERAL = "general"
    PRODUCT_FOCUS = "product_focus" # e.g. highlighting a specific dish or room
    SERVICE_FOCUS = "service_focus" # e.g. highlighting good service
    OTHER = "other"

class PromptTemplate(Base):
    __tablename__ = "prompt_templates"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False, unique=True, comment="Unique name for the template, e.g., 'Christmas Dinner Prompt'")
    category = Column(SQLAlchemyEnum(PromptTemplateCategoryEnum), nullable=False, default=PromptTemplateCategoryEnum.GENERAL, index=True)
    text = Column(Text, nullable=False, comment="The actual template text, possibly with placeholders like {{product_name}} or {{service_name}}")
    
    # No direct merchant_id FK here, as these are global templates.
    # Merchants might have a way to "favorite" or select from these,
    # or CustomAIPrompt might link to a base template.

    def __repr__(self):
        return f"<PromptTemplate(id={self.id}, name='{self.name}', category='{self.category.value}')>"
