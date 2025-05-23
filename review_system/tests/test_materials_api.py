import pytest
from httpx import AsyncClient
from fastapi import status

from app.schemas.material import FileTypeEnum

pytestmark = pytest.mark.asyncio # Applies to all tests in this module

async def test_create_material(client: AsyncClient):
    material_data = {
        "merchant_id": 1,
        "content_url": "http://example.com/image.jpg",
        "file_type": FileTypeEnum.IMAGE.value,
        "tags": "test, image"
    }
    response = await client.post("/management/materials/", json=material_data)
    assert response.status_code == status.HTTP_201_CREATED
    data = response.json()
    assert data["content_url"] == material_data["content_url"]
    assert data["merchant_id"] == material_data["merchant_id"]
    assert data["file_type"] == material_data["file_type"]
    assert "id" in data
    assert "upload_date" in data
    assert data["status"] == "unused"

async def test_create_material_invalid_url(client: AsyncClient):
    material_data = {
        "merchant_id": 1,
        "content_url": "not_a_url",
        "file_type": FileTypeEnum.IMAGE.value,
    }
    response = await client.post("/management/materials/", json=material_data)
    assert response.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY

async def test_read_material(client: AsyncClient):
    material_data = {
        "merchant_id": 2,
        "content_url": "http://example.com/another.png",
        "file_type": FileTypeEnum.IMAGE.value
    }
    create_response = await client.post("/management/materials/", json=material_data)
    created_id = create_response.json()["id"]

    response = await client.get(f"/management/materials/{created_id}")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["id"] == created_id

async def test_read_material_not_found(client: AsyncClient):
    response = await client.get("/management/materials/99999")
    assert response.status_code == status.HTTP_404_NOT_FOUND

async def test_read_materials_empty(client: AsyncClient):
    response = await client.get("/management/materials/?merchant_id=999")
    assert response.status_code == status.HTTP_200_OK
    assert response.json() == []

async def test_read_materials_with_data(client: AsyncClient):
    await client.post("/management/materials/", json={
        "merchant_id": 3, "content_url": "http://example.com/vid1.mp4", "file_type": "video", "tags": "v1"
    })
    await client.post("/management/materials/", json={
        "merchant_id": 3, "content_url": "http://example.com/vid2.mp4", "file_type": "video", "tags": "v2", "status": "used"
    })

    response = await client.get("/management/materials/?merchant_id=3")
    assert response.status_code == status.HTTP_200_OK
    assert len(response.json()) == 2

    response_status_filter = await client.get("/management/materials/?merchant_id=3&status=used")
    assert len(response_status_filter.json()) == 1

async def test_create_material_set(client: AsyncClient):
    set_data = {
        "merchant_id": 1,
        "name": "Summer Collection",
        "description": "Materials for summer promotions"
    }
    response = await client.post("/management/material_sets/", json=set_data)
    assert response.status_code == status.HTTP_201_CREATED
    data = response.json()
    assert data["name"] == set_data["name"]
    assert data["materials"] == []

async def test_add_material_to_set(client: AsyncClient):
    material_resp = await client.post("/management/materials/", json={
        "merchant_id": 10, "content_url": "http://example.com/item1.jpg", "file_type": "image"
    })
    material_id = material_resp.json()["id"]

    set_resp = await client.post("/management/material_sets/", json={
        "merchant_id": 10, "name": "Test Set"
    })
    set_id = set_resp.json()["id"]

    response = await client.post(f"/management/material_sets/{set_id}/materials/{material_id}")
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert len(data["materials"]) == 1
    assert data["materials"][0]["id"] == material_id

async def test_remove_material_from_set(client: AsyncClient):
    material_resp = await client.post("/management/materials/", json={
        "merchant_id": 11, "content_url": "http://example.com/item_to_remove.jpg", "file_type": "image"
    })
    material_id = material_resp.json()["id"]
    set_resp = await client.post("/management/material_sets/", json={
        "merchant_id": 11, "name": "Set For Removal Test"
    })
    set_id = set_resp.json()["id"]
    await client.post(f"/management/material_sets/{set_id}/materials/{material_id}")

    response = await client.delete(f"/management/material_sets/{set_id}/materials/{material_id}")
    assert response.status_code == status.HTTP_200_OK
    assert len(response.json()["materials"]) == 0
