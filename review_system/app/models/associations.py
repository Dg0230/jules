from sqlalchemy import Table, Column, Integer, ForeignKey
from app.models.base import Base

reviewtag_materialset_association = Table(
    'reviewtag_materialset_association', Base.metadata,
    Column('review_tag_id', Integer, ForeignKey('review_tags.id'), primary_key=True),
    Column('material_set_id', Integer, ForeignKey('materialsets.id'), primary_key=True)
)
