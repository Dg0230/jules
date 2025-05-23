from sqlalchemy import Column, Integer, Text, ForeignKey, JSON
from sqlalchemy.orm import relationship

from app.models.base import Base

class CustomAIPrompt(Base):
    __tablename__ = "custom_ai_prompts"

    id = Column(Integer, primary_key=True, index=True)
    
    # Link to a specific ReviewTag. A ReviewTag might have one custom prompt.
    # If a ReviewTag can have multiple custom prompts (e.g., A/B testing, different versions),
    # this would be a one-to-many from ReviewTag to CustomAIPrompt, and ReviewTag.custom_ai_prompts would be a list.
    # For now, let's assume one-to-one for simplicity, meaning a ReviewTag has at most one custom prompt.
    # The 'unique=True' constraint on review_tag_id enforces this one-to-one nature.
    review_tag_id = Column(Integer, ForeignKey("review_tags.id"), nullable=False, unique=True, index=True)
    
    prompt_text = Column(Text, nullable=True, comment="Custom prompt text. Can be null if using style_config primarily.")
    
    # Using JSON for flexible style configurations, e.g., {"tone": "enthusiastic", "length": "short"}
    # This matches the "自定义AI提示词配置（针对不同好评牌定制文案风格，默认通用）" requirement.
    style_config = Column(JSON, nullable=True, comment="JSON blob for specific AI style configurations or parameters.")

    # Relationship to ReviewTag
    review_tag = relationship("ReviewTag", back_populates="custom_ai_prompt") # Note: singular 'custom_ai_prompt'

    def __repr__(self):
        return f"<CustomAIPrompt(id={self.id}, review_tag_id={self.review_tag_id})>"
