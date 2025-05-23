from typing import Optional, List # Ensure List is imported if not already
from pydantic import BaseModel

# Shared properties
class MerchantBase(BaseModel):
    name: str
    contact_info: Optional[str] = None
    channel_partner_id: Optional[int] = None # Add this

# Properties to receive on item creation
class MerchantCreate(MerchantBase):
    # channel_partner_id is now inherited and can be provided during creation
    pass

# Properties to receive on item update
class MerchantUpdate(BaseModel): # Re-define or ensure it has all updatable fields
    name: Optional[str] = None
    contact_info: Optional[str] = None
    channel_partner_id: Optional[int] = None # Add if updatable

# Properties shared by models stored in DB
class MerchantInDBBase(MerchantBase): # Inherits channel_partner_id
    id: int

    class Config:
        orm_mode = True

# Properties to return to client
class MerchantResponse(MerchantInDBBase): # Inherits channel_partner_id
    # Optionally, you could add nested ChannelPartner details here later if needed
    # channel_partner: Optional[ChannelPartnerResponse] = None 
    pass

# Properties stored in DB
class MerchantInDB(MerchantInDBBase): # Inherits channel_partner_id
    pass
