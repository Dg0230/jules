from sqlalchemy import Column, Integer, String, Text, Numeric
from sqlalchemy.orm import relationship # Ensure this is imported

from app.models.base import Base

class ChannelPartner(Base):
    __tablename__ = "channel_partners"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False, unique=True, index=True)
    contact_details = Column(Text, nullable=True, comment="Contact info like email, phone, address etc.")
    current_balance_for_payout = Column(Numeric(12, 2), nullable=False, default=0.00, comment="Current balance available for payout to the partner")
    total_profit_shared = Column(Numeric(12, 2), nullable=False, default=0.00, comment="Cumulative profit shared with this partner")

    # Define relationship to Merchants (One ChannelPartner to Many Merchants)
    merchants = relationship("Merchant", back_populates="channel_partner", order_by="Merchant.name") # Optional: order merchants by name

    def __repr__(self):
        return f"<ChannelPartner(id={self.id}, name='{self.name}')>"
