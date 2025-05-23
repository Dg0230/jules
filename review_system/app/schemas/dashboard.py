from pydantic import BaseModel
from decimal import Decimal

class MerchantDashboardResponse(BaseModel):
    positive_review_quantity: int
    top_up_amount: Decimal
    account_balance: Decimal
    verified_review_screenshot_quantity: int
    total_material_quantity: int
    remaining_material_quantity: int

    class Config:
        orm_mode = True
