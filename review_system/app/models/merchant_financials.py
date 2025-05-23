from sqlalchemy import Column, Integer, Numeric, ForeignKey
from sqlalchemy.orm import relationship

from app.models.base import Base

class MerchantFinancials(Base):
    __tablename__ = "merchant_financials"

    id = Column(Integer, primary_key=True, index=True)
    merchant_id = Column(Integer, ForeignKey("merchants.id"), nullable=False, unique=True, index=True) # One-to-one with Merchant

    # Using Numeric for precision, suitable for currency.
    # Precision and scale can be adjusted as needed. e.g., Numeric(10, 2) for up to 9,999,999.99
    account_balance = Column(Numeric(12, 2), nullable=False, default=0.00)
    total_top_up_amount = Column(Numeric(12, 2), nullable=False, default=0.00)

    # Relationship to Merchant
    merchant = relationship("Merchant", back_populates="financials")

    def __repr__(self):
        return f"<MerchantFinancials(id={self.id}, merchant_id={self.merchant_id}, balance={self.account_balance})>"
