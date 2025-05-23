from typing import Optional
from pydantic import BaseModel, condecimal

# Use condecimal for decimal type validation if needed, or float for simplicity if appropriate
DecimalPositive = condecimal(ge=0, decimal_places=2) # Example: positive decimal with 2 places

class MerchantFinancialsBase(BaseModel):
    account_balance: DecimalPositive = 0.00
    total_top_up_amount: DecimalPositive = 0.00

class MerchantFinancialsCreate(MerchantFinancialsBase):
    merchant_id: int # Required when creating directly, though often created alongside merchant or via specific ops

class MerchantFinancialsUpdate(BaseModel): # Separate update schema
    account_balance: Optional[DecimalPositive] = None
    total_top_up_amount: Optional[DecimalPositive] = None

class MerchantFinancialsResponse(MerchantFinancialsBase):
    id: int
    merchant_id: int

    class Config:
        orm_mode = True
