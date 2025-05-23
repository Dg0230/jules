from typing import Optional
from pydantic import BaseModel, condecimal 
from decimal import Decimal 

DecimalPositiveOrZero = condecimal(ge=Decimal('0.00'), decimal_places=2)

class ChannelPartnerBase(BaseModel):
    name: str
    contact_details: Optional[str] = None

class ChannelPartnerCreate(ChannelPartnerBase):
    pass

class ChannelPartnerUpdate(BaseModel): 
    name: Optional[str] = None
    contact_details: Optional[str] = None
    current_balance_for_payout: Optional[DecimalPositiveOrZero] = None
    total_profit_shared: Optional[DecimalPositiveOrZero] = None

class ChannelPartnerResponse(ChannelPartnerBase):
    id: int
    current_balance_for_payout: DecimalPositiveOrZero
    total_profit_shared: DecimalPositiveOrZero

    class Config:
        orm_mode = True

class ChannelPartnerFinancialsSummaryResponse(BaseModel): # New Schema
    current_balance_for_payout: DecimalPositiveOrZero
    total_profit_shared: DecimalPositiveOrZero

    class Config:
        orm_mode = True
