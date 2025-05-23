import pytest
from httpx import AsyncClient
from fastapi import status
from sqlalchemy.ext.asyncio import AsyncSession

from app.schemas.merchant import MerchantCreate
from app.schemas.material import MaterialSetCreate
from app.schemas.review_tag import ReviewTagCreate, ReviewTagUpdate, ReviewTagStatusEnum
from app.schemas.custom_ai_prompt import CustomAIPromptBase # Added
from app.crud import crud_merchant, crud_material, crud_review_tag, crud_custom_ai_prompt # Added

pytestmark = pytest.mark.asyncio

async def create_merchant_and_material_sets(db_session: AsyncSession, merchant_name: str, num_sets: int = 1):
    merchant = await crud_merchant.create_merchant(db_session, MerchantCreate(name=merchant_name))
    material_sets = []
    for i in range(num_sets):
        ms_data = MaterialSetCreate(merchant_id=merchant.id, name=f"Set {i+1} for {merchant_name}")
        ms = await crud_material.create_material_set(db_session, material_set=ms_data)
        material_sets.append(ms)
    return merchant, material_sets

# Test POST /review_tags/
async def test_create_review_tag_success(client: AsyncClient, db_session: AsyncSession):
    merchant, material_sets = await create_merchant_and_material_sets(db_session, "TagMerchant1", num_sets=2)
    tag_data = {
        "merchant_id": merchant.id,
        "name": "Summer Sale Tag",
        "status": ReviewTagStatusEnum.ACTIVE.value,
        "material_set_ids": [ms.id for ms in material_sets]
    }
    response = await client.post("/review_tags/", json=tag_data)
    assert response.status_code == status.HTTP_201_CREATED
    data = response.json()
    assert data["name"] == tag_data["name"]
    assert data["merchant_id"] == merchant.id
    assert len(data["material_sets"]) == len(material_sets)
    assert data["material_sets"][0]["id"] == material_sets[0].id

async def test_create_review_tag_non_existent_merchant(client: AsyncClient):
    tag_data = {"merchant_id": 99999, "name": "Tag for NonExistent Merchant"}
    response = await client.post("/review_tags/", json=tag_data)
    assert response.status_code == status.HTTP_400_BAD_REQUEST 
    assert "Merchant with id 99999 not found" in response.json()["detail"]

async def test_create_review_tag_invalid_material_set_id(client: AsyncClient, db_session: AsyncSession):
    merchant, _ = await create_merchant_and_material_sets(db_session, "TagMerchant2")
    tag_data = {
        "merchant_id": merchant.id,
        "name": "Tag with Invalid MS",
        "material_set_ids": [98765] 
    }
    response = await client.post("/review_tags/", json=tag_data)
    assert response.status_code == status.HTTP_400_BAD_REQUEST
    assert "One or more MaterialSet IDs are invalid or not found" in response.json()["detail"]

async def test_create_review_tag_material_set_wrong_merchant(client: AsyncClient, db_session: AsyncSession):
    merchant1, material_sets1 = await create_merchant_and_material_sets(db_session, "TagMerchant3_M1")
    merchant2, _ = await create_merchant_and_material_sets(db_session, "TagMerchant3_M2")
    
    tag_data = {
        "merchant_id": merchant2.id, 
        "name": "Tag with M1's MS",
        "material_set_ids": [material_sets1[0].id] 
    }
    response = await client.post("/review_tags/", json=tag_data)
    assert response.status_code == status.HTTP_400_BAD_REQUEST
    assert f"MaterialSet id {material_sets1[0].id} does not belong to merchant {merchant2.id}" in response.json()["detail"]


# Test GET /merchants/{merchant_id}/review_tags/
async def test_list_review_tags_for_merchant(client: AsyncClient, db_session: AsyncSession):
    merchant, material_sets = await create_merchant_and_material_sets(db_session, "ListTagMerchant", num_sets=1)
    await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag 1", material_set_ids=[material_sets[0].id]))
    await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag 2"))

    response = await client.get(f"/merchants/{merchant.id}/review_tags/")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert len(data) == 2
    assert data[0]["name"] == "Tag 1" 

async def test_list_review_tags_non_existent_merchant(client: AsyncClient):
    response = await client.get("/merchants/99999/review_tags/")
    assert response.status_code == status.HTTP_404_NOT_FOUND

async def test_list_review_tags_pagination(client: AsyncClient, db_session: AsyncSession):
    merchant, _ = await create_merchant_and_material_sets(db_session, "PaginateTagMerchant")
    for i in range(5):
        await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name=f"Tag P{i}"))
    
    response_limit_2 = await client.get(f"/merchants/{merchant.id}/review_tags/?limit=2")
    assert len(response_limit_2.json()) == 2
    response_skip_2_limit_2 = await client.get(f"/merchants/{merchant.id}/review_tags/?skip=2&limit=2")
    assert len(response_skip_2_limit_2.json()) == 2


# Test GET /review_tags/{tag_id}
async def test_get_review_tag_by_id_success(client: AsyncClient, db_session: AsyncSession):
    merchant, material_sets = await create_merchant_and_material_sets(db_session, "GetTagMerchant", num_sets=1)
    created_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Specific Tag", material_set_ids=[material_sets[0].id]))

    response = await client.get(f"/review_tags/{created_tag.id}")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["id"] == created_tag.id
    assert data["name"] == "Specific Tag"
    assert len(data["material_sets"]) == 1
    assert data["material_sets"][0]["id"] == material_sets[0].id

async def test_get_review_tag_non_existent(client: AsyncClient):
    response = await client.get("/review_tags/99999")
    assert response.status_code == status.HTTP_404_NOT_FOUND


# Test PUT /review_tags/{tag_id}
async def test_update_review_tag_success(client: AsyncClient, db_session: AsyncSession):
    merchant, material_sets = await create_merchant_and_material_sets(db_session, "UpdateTagMerchant", num_sets=2)
    ms1, ms2 = material_sets
    created_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Old Name", material_set_ids=[ms1.id]))

    update_data = {
        "name": "New Updated Name",
        "status": ReviewTagStatusEnum.INACTIVE.value,
        "material_set_ids": [ms2.id] 
    }
    response = await client.put(f"/review_tags/{created_tag.id}", json=update_data)
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["name"] == "New Updated Name"
    assert data["status"] == ReviewTagStatusEnum.INACTIVE.value
    assert len(data["material_sets"]) == 1
    assert data["material_sets"][0]["id"] == ms2.id

async def test_update_review_tag_clear_material_sets(client: AsyncClient, db_session: AsyncSession):
    merchant, material_sets = await create_merchant_and_material_sets(db_session, "ClearSetTagMerchant", num_sets=1)
    created_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag with Set", material_set_ids=[material_sets[0].id]))
    
    update_data = {"material_set_ids": []} 
    response = await client.put(f"/review_tags/{created_tag.id}", json=update_data)
    assert response.status_code == status.HTTP_200_OK
    assert len(response.json()["material_sets"]) == 0

async def test_update_review_tag_invalid_ms_id(client: AsyncClient, db_session: AsyncSession):
    merchant, _ = await create_merchant_and_material_sets(db_session, "UpdateTagInvalidMS")
    created_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag to Update"))
    
    update_data = {"material_set_ids": [98765]}
    response = await client.put(f"/review_tags/{created_tag.id}", json=update_data)
    assert response.status_code == status.HTTP_400_BAD_REQUEST
    assert "One or more MaterialSet IDs for update are invalid or not found" in response.json()["detail"]

async def test_update_review_tag_non_existent(client: AsyncClient):
    response = await client.put("/review_tags/99999", json={"name": "Attempt Update"})
    assert response.status_code == status.HTTP_404_NOT_FOUND

# Test DELETE /review_tags/{tag_id}
async def test_delete_review_tag_success(client: AsyncClient, db_session: AsyncSession):
    merchant, _ = await create_merchant_and_material_sets(db_session, "DeleteTagMerchant")
    created_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag to Delete"))

    response = await client.delete(f"/review_tags/{created_tag.id}")
    assert response.status_code == status.HTTP_200_OK
    assert response.json()["id"] == created_tag.id

    get_response = await client.get(f"/review_tags/{created_tag.id}")
    assert get_response.status_code == status.HTTP_404_NOT_FOUND

async def test_delete_review_tag_non_existent(client: AsyncClient):
    response = await client.delete("/review_tags/99999")
    assert response.status_code == status.HTTP_404_NOT_FOUND

# Tests for Custom AI Prompts (nested under /review_tags/{tag_id}/custom_prompt/)

async def test_create_or_replace_custom_prompt_create_new(client: AsyncClient, db_session: AsyncSession):
    merchant, _ = await create_merchant_and_material_sets(db_session, "PromptMerchant1")
    review_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag for Prompt"))
    
    prompt_data = {"prompt_text": "New custom prompt text", "style_config": {"tone": "friendly"}}
    response = await client.post(f"/review_tags/{review_tag.id}/custom_prompt/", json=prompt_data)
    assert response.status_code == status.HTTP_201_CREATED
    data = response.json()
    assert data["prompt_text"] == prompt_data["prompt_text"]
    assert data["style_config"]["tone"] == "friendly"
    assert data["review_tag_id"] == review_tag.id

async def test_create_or_replace_custom_prompt_replace_existing(client: AsyncClient, db_session: AsyncSession):
    merchant, _ = await create_merchant_and_material_sets(db_session, "PromptMerchant2")
    review_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag for Prompt Update"))
    
    # Create initial prompt
    initial_prompt_data = {"prompt_text": "Initial prompt", "style_config": {"tone": "neutral"}}
    await client.post(f"/review_tags/{review_tag.id}/custom_prompt/", json=initial_prompt_data)
    
    # Replace with new prompt data
    replace_prompt_data = {"prompt_text": "Replaced prompt text", "style_config": {"length": "short"}}
    response = await client.post(f"/review_tags/{review_tag.id}/custom_prompt/", json=replace_prompt_data)
    assert response.status_code == status.HTTP_200_OK # API endpoint returns 200 on update
    data = response.json()
    assert data["prompt_text"] == replace_prompt_data["prompt_text"]
    assert data["style_config"]["length"] == "short"
    assert "tone" not in data["style_config"] # Should replace, not merge, style_config by default Pydantic model update

async def test_create_custom_prompt_for_non_existent_tag(client: AsyncClient):
    prompt_data = {"prompt_text": "Prompt for non-existent tag"}
    response = await client.post("/review_tags/99999/custom_prompt/", json=prompt_data)
    assert response.status_code == status.HTTP_404_NOT_FOUND
    assert "ReviewTag not found" in response.json()["detail"]

# Test GET /review_tags/{tag_id}/custom_prompt/
async def test_get_custom_prompt_success(client: AsyncClient, db_session: AsyncSession):
    merchant, _ = await create_merchant_and_material_sets(db_session, "GetPromptMerchant")
    review_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag with Prompt"))
    prompt_data = CustomAIPromptBase(prompt_text="My prompt", style_config={"detail": "high"})
    # Direct CRUD call to create the prompt associated with the tag
    await crud_custom_ai_prompt.create_custom_ai_prompt(db_session, review_tag_id=review_tag.id, prompt_in=prompt_data)


    response = await client.get(f"/review_tags/{review_tag.id}/custom_prompt/")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["prompt_text"] == "My prompt"
    assert data["style_config"]["detail"] == "high"

async def test_get_custom_prompt_for_tag_without_prompt(client: AsyncClient, db_session: AsyncSession):
    merchant, _ = await create_merchant_and_material_sets(db_session, "TagNoPromptMerchant")
    review_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag without Prompt"))
    
    response = await client.get(f"/review_tags/{review_tag.id}/custom_prompt/")
    assert response.status_code == status.HTTP_404_NOT_FOUND
    assert "CustomAIPrompt not found for this ReviewTag" in response.json()["detail"]

async def test_get_custom_prompt_for_non_existent_tag(client: AsyncClient):
    response = await client.get("/review_tags/99999/custom_prompt/")
    # This will first hit the `get_review_tag` in the endpoint, which should 404 if tag doesn't exist.
    # If the endpoint logic changes, this might need adjustment.
    # The current endpoint logic in `app/api/endpoints/review_tags.py` for GET /review_tags/{tag_id}/custom_prompt/
    # directly calls `crud_custom_ai_prompt.get_custom_ai_prompt_by_review_tag_id`.
    # It doesn't explicitly check if the tag_id itself is valid first.
    # So, if the tag doesn't exist, the prompt won't be found either.
    assert response.status_code == status.HTTP_404_NOT_FOUND 
    assert "CustomAIPrompt not found for this ReviewTag" in response.json()["detail"]


# Test DELETE /review_tags/{tag_id}/custom_prompt/
async def test_delete_custom_prompt_success(client: AsyncClient, db_session: AsyncSession):
    merchant, _ = await create_merchant_and_material_sets(db_session, "DeletePromptMerchant")
    review_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag for Deleting Prompt"))
    prompt_data = CustomAIPromptBase(prompt_text="To be deleted")
    created_prompt = await crud_custom_ai_prompt.create_custom_ai_prompt(db_session, review_tag_id=review_tag.id, prompt_in=prompt_data)

    response = await client.delete(f"/review_tags/{review_tag.id}/custom_prompt/")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["id"] == created_prompt.id # Check if the deleted prompt data is returned

    # Verify it's deleted
    get_response = await client.get(f"/review_tags/{review_tag.id}/custom_prompt/")
    assert get_response.status_code == status.HTTP_404_NOT_FOUND

async def test_delete_custom_prompt_non_existent_prompt(client: AsyncClient, db_session: AsyncSession):
    merchant, _ = await create_merchant_and_material_sets(db_session, "DeleteNonPromptMerchant")
    review_tag = await crud_review_tag.create_review_tag(db_session, ReviewTagCreate(merchant_id=merchant.id, name="Tag with No Prompt to Delete"))
    
    response = await client.delete(f"/review_tags/{review_tag.id}/custom_prompt/")
    assert response.status_code == status.HTTP_404_NOT_FOUND
    assert "CustomAIPrompt not found for this ReviewTag to delete" in response.json()["detail"]

async def test_delete_custom_prompt_for_non_existent_tag(client: AsyncClient):
    response = await client.delete("/review_tags/99999/custom_prompt/")
    assert response.status_code == status.HTTP_404_NOT_FOUND
    assert "ReviewTag not found" in response.json()["detail"] # Parent tag check first in API endpoint
