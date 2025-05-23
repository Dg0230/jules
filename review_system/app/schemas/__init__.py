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
from .dashboard import MerchantDashboardResponse # Add this line

__all__ = [
    "MaterialBase", "MaterialCreate", "MaterialUpdate", "MaterialResponse",
    "MaterialSetBase", "MaterialSetCreate", "MaterialSetUpdate", "MaterialSetResponse",
    "MaterialSetAssociation",
    "MerchantBase", "MerchantCreate", "MerchantUpdate", "MerchantResponse", "MerchantInDB",
    "MerchantFinancialsBase", "MerchantFinancialsCreate", 
    "MerchantFinancialsUpdate", "MerchantFinancialsResponse",
    "ReviewBase", "ReviewCreate", "ReviewUpdate", "ReviewResponse",
    "MerchantDashboardResponse" # Add this
]
