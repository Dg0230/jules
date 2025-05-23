import pytest
from sqlalchemy.ext.asyncio import AsyncSession
from app.crud import crud_merchant, crud_review
from app.schemas.merchant import MerchantCreate
from app.schemas.review import ReviewCreate, ReviewUpdate
from app.models.review import ReviewPlatformEnum

pytestmark = pytest.mark.asyncio

async def test_create_review(db_session: AsyncSession):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="ReviewLogic Merchant"))
    review_data = {
        "merchant_id": merchant.id,
        "platform": ReviewPlatformEnum.MEITUAN,
        "is_positive": True,
        "screenshot_url": "http://example.com/review.jpg"
    }
    created_review = await crud_review.create_review(db_session, ReviewCreate(**review_data))
    assert created_review is not None
    assert created_review.merchant_id == merchant.id
    assert created_review.platform == ReviewPlatformEnum.MEITUAN
    assert created_review.screenshot_url == "http://example.com/review.jpg"

async def test_create_review_non_existent_merchant(db_session: AsyncSession):
    review_data = {
        "merchant_id": 99999, # Non-existent merchant
        "platform": ReviewPlatformEnum.CTRIP,
        "is_positive": True
    }
    with pytest.raises(ValueError, match="Merchant with id 99999 not found."):
        await crud_review.create_review(db_session, ReviewCreate(**review_data))

async def test_get_review(db_session: AsyncSession):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="GetReview Merchant"))
    review_in = ReviewCreate(merchant_id=merchant.id, platform=ReviewPlatformEnum.XIAOHONGSHU)
    created_review = await crud_review.create_review(db_session, review_in)
    
    fetched_review = await crud_review.get_review(db_session, created_review.id)
    assert fetched_review is not None
    assert fetched_review.id == created_review.id
    assert fetched_review.platform == ReviewPlatformEnum.XIAOHONGSHU

async def test_get_reviews_by_merchant_with_filters(db_session: AsyncSession):
    merchant1 = await crud_merchant.create_merchant(db_session, MerchantCreate(name="FilterReview Merchant1"))
    merchant2 = await crud_merchant.create_merchant(db_session, MerchantCreate(name="FilterReview Merchant2"))

    # Reviews for merchant1
    await crud_review.create_review(db_session, ReviewCreate(merchant_id=merchant1.id, platform=ReviewPlatformEnum.MEITUAN, is_positive=True, is_verified=True))
    await crud_review.create_review(db_session, ReviewCreate(merchant_id=merchant1.id, platform=ReviewPlatformEnum.CTRIP, is_positive=True, is_verified=False))
    await crud_review.create_review(db_session, ReviewCreate(merchant_id=merchant1.id, platform=ReviewPlatformEnum.MEITUAN, is_positive=False, is_verified=True))
    
    # Review for merchant2 (should not appear in merchant1's list)
    await crud_review.create_review(db_session, ReviewCreate(merchant_id=merchant2.id, platform=ReviewPlatformEnum.MEITUAN, is_positive=True))

    # Test: Get all for merchant1
    reviews_m1_all = await crud_review.get_reviews_by_merchant(db_session, merchant_id=merchant1.id, limit=10)
    assert len(reviews_m1_all) == 3

    # Test: Filter by platform for merchant1
    reviews_m1_meituan = await crud_review.get_reviews_by_merchant(db_session, merchant_id=merchant1.id, platform=ReviewPlatformEnum.MEITUAN, limit=10)
    assert len(reviews_m1_meituan) == 2

    # Test: Filter by is_positive for merchant1
    reviews_m1_positive = await crud_review.get_reviews_by_merchant(db_session, merchant_id=merchant1.id, is_positive=True, limit=10)
    assert len(reviews_m1_positive) == 2
    
    # Test: Filter by is_verified for merchant1
    reviews_m1_verified = await crud_review.get_reviews_by_merchant(db_session, merchant_id=merchant1.id, is_verified=True, limit=10)
    assert len(reviews_m1_verified) == 2
    
    # Test: Combined filters for merchant1
    reviews_m1_combined = await crud_review.get_reviews_by_merchant(db_session, merchant_id=merchant1.id, platform=ReviewPlatformEnum.MEITUAN, is_positive=True, is_verified=True, limit=10)
    assert len(reviews_m1_combined) == 1


async def test_update_review(db_session: AsyncSession):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="UpdateReview Merchant"))
    review_in = ReviewCreate(merchant_id=merchant.id, platform=ReviewPlatformEnum.OTHER, is_verified=False, screenshot_url="http://example.com/old.jpg")
    created_review = await crud_review.create_review(db_session, review_in)
    
    update_data = ReviewUpdate(is_verified=True, screenshot_url="http://example.com/new.jpg", is_positive=False)
    updated_review = await crud_review.update_review(db_session, review_id=created_review.id, review_update=update_data)
    
    assert updated_review is not None
    assert updated_review.is_verified is True
    assert updated_review.is_positive is False
    assert updated_review.screenshot_url == "http://example.com/new.jpg"
    assert updated_review.platform == ReviewPlatformEnum.OTHER # Should not change

async def test_delete_review(db_session: AsyncSession):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="DeleteReview Merchant"))
    review_in = ReviewCreate(merchant_id=merchant.id, platform=ReviewPlatformEnum.DAZHONG_DIANPING)
    created_review = await crud_review.create_review(db_session, review_in)
    
    deleted_review = await crud_review.delete_review(db_session, created_review.id)
    assert deleted_review is not None
    assert deleted_review.id == created_review.id
    
    fetched_review_after_delete = await crud_review.get_review(db_session, created_review.id)
    assert fetched_review_after_delete is None

async def test_get_reviews_count_by_merchant(db_session: AsyncSession):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name="ReviewCountLogic Merchant"))
    
    await crud_review.create_review(db_session, ReviewCreate(merchant_id=merchant.id, platform=ReviewPlatformEnum.MEITUAN, is_positive=True, is_verified=True))
    await crud_review.create_review(db_session, ReviewCreate(merchant_id=merchant.id, platform=ReviewPlatformEnum.CTRIP, is_positive=True, is_verified=False))
    await crud_review.create_review(db_session, ReviewCreate(merchant_id=merchant.id, platform=ReviewPlatformEnum.OTHER, is_positive=False, is_verified=True))

    total_reviews = await crud_review.get_reviews_count_by_merchant(db_session, merchant_id=merchant.id)
    assert total_reviews == 3
    
    positive_verified_reviews = await crud_review.get_reviews_count_by_merchant(db_session, merchant_id=merchant.id, is_positive=True, is_verified=True)
    assert positive_verified_reviews == 1

    positive_reviews = await crud_review.get_reviews_count_by_merchant(db_session, merchant_id=merchant.id, is_positive=True)
    assert positive_reviews == 2

    verified_reviews = await crud_review.get_reviews_count_by_merchant(db_session, merchant_id=merchant.id, is_verified=True)
    assert verified_reviews == 2
    
    negative_verified_reviews = await crud_review.get_reviews_count_by_merchant(db_session, merchant_id=merchant.id, is_positive=False, is_verified=True)
    assert negative_verified_reviews == 1
