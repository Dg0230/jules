from typing import Optional, List
from pydantic import BaseModel

# Shared properties
class MerchantBase(BaseModel):
    name: str
    contact_info: Optional[str] = None

# Properties to receive on item creation
class MerchantCreate(MerchantBase):
    pass

# Properties to receive on item update
class MerchantUpdate(MerchantBase):
    name: Optional[str] = None # All fields optional for update
    contact_info: Optional[str] = None

# Properties shared by models stored in DB
class MerchantInDBBase(MerchantBase):
    id: int

    class Config:
        orm_mode = True

# Properties to return to client
class MerchantResponse(MerchantInDBBase):
    pass

# Properties stored in DB
class MerchantInDB(MerchantInDBBase):
    pass
