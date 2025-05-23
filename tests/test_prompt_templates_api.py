import pytest
from httpx import AsyncClient
from fastapi import status
from sqlalchemy.ext.asyncio import AsyncSession

from app.crud import crud_prompt_template
from app.schemas.prompt_template import PromptTemplateCreate, PromptTemplateCategoryEnum

pytestmark = pytest.mark.asyncio

# Helper to create prompt templates for testing GET endpoints
async def create_sample_templates(db_session: AsyncSession):
    templates_data = [
        {"name": "General Greeting", "category": PromptTemplateCategoryEnum.GENERAL, "text": "Hello there!"},
        {"name": "Holiday Special Xmas", "category": PromptTemplateCategoryEnum.HOLIDAY, "text": "Merry Christmas!"},
        {"name": "Summer Vibes", "category": PromptTemplateCategoryEnum.SEASONAL, "text": "Enjoy the summer!"},
        {"name": "New Product Launch", "category": PromptTemplateCategoryEnum.PRODUCT_FOCUS, "text": "Check out our new {{product_name}}!"},
        {"name": "General Event", "category": PromptTemplateCategoryEnum.EVENT, "text": "Join our special event!"},
    ]
    created_templates = []
    for td in templates_data:
        template = await crud_prompt_template.create_prompt_template(db_session, PromptTemplateCreate(**td))
        created_templates.append(template)
    return created_templates

# Test GET /prompt_templates/
async def test_list_all_prompt_templates(client: AsyncClient, db_session: AsyncSession):
    await create_sample_templates(db_session) # Ensure some data exists
    
    response = await client.get("/prompt_templates/") # Base URL /api/v1 is in client fixture
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert isinstance(data, list)
    # Check if at least the number of created templates are returned. 
    # Could be more if DB is not fully cleaned between test runs or other tests add templates.
    assert len(data) >= 5 

async def test_list_prompt_templates_with_category_filter(client: AsyncClient, db_session: AsyncSession):
    await create_sample_templates(db_session)

    response = await client.get(f"/prompt_templates/?category={PromptTemplateCategoryEnum.HOLIDAY.value}")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert isinstance(data, list)
    assert len(data) >= 1 # At least one holiday template was added
    for template in data:
        assert template["category"] == PromptTemplateCategoryEnum.HOLIDAY.value

async def test_list_prompt_templates_pagination(client: AsyncClient, db_session: AsyncSession):
    await create_sample_templates(db_session) # Adds 5 templates

    response_limit_2 = await client.get("/prompt_templates/?limit=2")
    assert response_limit_2.status_code == status.HTTP_200_OK
    assert len(response_limit_2.json()) == 2

    response_skip_2_limit_2 = await client.get("/prompt_templates/?skip=2&limit=2")
    assert response_skip_2_limit_2.status_code == status.HTTP_200_OK
    assert len(response_skip_2_limit_2.json()) == 2

# Test GET /prompt_templates/{template_id}
async def test_get_prompt_template_by_id_success(client: AsyncClient, db_session: AsyncSession):
    created_templates = await create_sample_templates(db_session)
    if not created_templates:
        pytest.fail("Test setup failed: No templates created.")
        
    template_to_fetch = created_templates[0]
    
    response = await client.get(f"/prompt_templates/{template_to_fetch.id}")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["id"] == template_to_fetch.id
    assert data["name"] == template_to_fetch.name
    assert data["text"] == template_to_fetch.text

async def test_get_prompt_template_non_existent(client: AsyncClient):
    response = await client.get("/prompt_templates/99999")
    assert response.status_code == status.HTTP_404_NOT_FOUND

# Optional: Direct CRUD tests (if admin APIs are not yet implemented)
# These are not strictly API tests but test the CRUD layer for completion.
# If these functionalities are only for admin use, they might be tested via admin API tests later.

async def test_crud_create_prompt_template(db_session: AsyncSession):
    template_data = PromptTemplateCreate(name="CRUD Test Template", category=PromptTemplateCategoryEnum.GENERAL, text="Test text.")
    created = await crud_prompt_template.create_prompt_template(db_session, template_data)
    assert created.name == template_data.name
    assert created.id is not None

async def test_crud_update_prompt_template(db_session: AsyncSession):
    template_data = PromptTemplateCreate(name="CRUD Update Original", category=PromptTemplateCategoryEnum.GENERAL, text="Original text.")
    created = await crud_prompt_template.create_prompt_template(db_session, template_data)
    
    from app.schemas.prompt_template import PromptTemplateUpdate
    update_data = PromptTemplateUpdate(name="CRUD Updated Name", text="Updated text.")
    updated = await crud_prompt_template.update_prompt_template(db_session, created.id, update_data)
    assert updated.name == "CRUD Updated Name"
    assert updated.text == "Updated text."

async def test_crud_delete_prompt_template(db_session: AsyncSession):
    template_data = PromptTemplateCreate(name="CRUD Delete Template", category=PromptTemplateCategoryEnum.GENERAL, text="Delete me.")
    created = await crud_prompt_template.create_prompt_template(db_session, template_data)
    
    deleted = await crud_prompt_template.delete_prompt_template(db_session, created.id)
    assert deleted.id == created.id
    
    refetched = await crud_prompt_template.get_prompt_template(db_session, created.id)
    assert refetched is None
