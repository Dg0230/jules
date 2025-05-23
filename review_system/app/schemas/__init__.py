from .material import (
    MaterialBase, MaterialCreate, MaterialUpdate, MaterialResponse,
    MaterialSetBase, MaterialSetCreate, MaterialSetUpdate, MaterialSetResponse,
    MaterialSetAssociation
)
from .merchant import (
    MerchantBase, MerchantCreate, MerchantUpdate, MerchantResponse, MerchantInDB 
)
from .merchant_financials import (
    MerchantFinancialsBase, MerchantFinancialsCreate, 
    MerchantFinancialsUpdate, MerchantFinancialsResponse
)
from .review import (
    ReviewBase, ReviewCreate, ReviewUpdate, ReviewResponse
)
from .dashboard import MerchantDashboardResponse
from .review_tag import (
    ReviewTagBase, ReviewTagCreate, ReviewTagUpdate, ReviewTagResponse
)
from .prompt_template import (
    PromptTemplateBase, PromptTemplateCreate, 
    PromptTemplateUpdate, PromptTemplateResponse
)
from .custom_ai_prompt import (
    CustomAIPromptBase, CustomAIPromptCreate,
    CustomAIPromptUpdate, CustomAIPromptResponse
)
from .channel_partner import ( 
    ChannelPartnerBase, ChannelPartnerCreate,
    ChannelPartnerUpdate, ChannelPartnerResponse,
    ChannelPartnerFinancialsSummaryResponse # New schema
)


__all__ = [
    "MaterialBase", "MaterialCreate", "MaterialUpdate", "MaterialResponse",
    "MaterialSetBase", "MaterialSetCreate", "MaterialSetUpdate", "MaterialSetResponse",
    "MaterialSetAssociation",
    "MerchantBase", "MerchantCreate", "MerchantUpdate", "MerchantResponse", "MerchantInDB",
    "MerchantFinancialsBase", "MerchantFinancialsCreate", 
    "MerchantFinancialsUpdate", "MerchantFinancialsResponse",
    "ReviewBase", "ReviewCreate", "ReviewUpdate", "ReviewResponse",
    "MerchantDashboardResponse",
    "ReviewTagBase", "ReviewTagCreate", "ReviewTagUpdate", "ReviewTagResponse", # Added ReviewTagBase for completeness
    "PromptTemplateBase", "PromptTemplateCreate", 
    "PromptTemplateUpdate", "PromptTemplateResponse",
    "CustomAIPromptBase", "CustomAIPromptCreate",
    "CustomAIPromptUpdate", "CustomAIPromptResponse",
    "ChannelPartnerBase", "ChannelPartnerCreate", 
    "ChannelPartnerUpdate", "ChannelPartnerResponse",
    "ChannelPartnerFinancialsSummaryResponse" 
]
