const API_BASE_URL = (
  window.__APP_CONFIG__?.API_BASE_URL ??
  window.location.origin
).replace(/\/$/, "");

export async function loadPhotoMarkers() {
  const response = await fetch(`${API_BASE_URL}/api/map/photos`, {
    headers: {
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`API responded with ${response.status}`);
  }

  const payload = await response.json();
  const items = Array.isArray(payload) ? payload : payload.items;

  if (!Array.isArray(items)) {
    throw new Error("Unexpected marker payload");
  }

  return items.map(adaptMarkerDto);
}

export async function loadPhotoDetails(photoId) {
  const response = await fetch(`${API_BASE_URL}/api/photos/${photoId}`, {
    headers: {
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`API responded with ${response.status}`);
  }

  return adaptPhotoDetailsDto(await response.json());
}

function adaptMarkerDto(dto) {
  return {
    id: dto.id,
    lat: dto.latitude,
    lng: dto.longitude,
  };
}

function adaptPhotoDetailsDto(dto) {
  return {
    id: dto.id,
    title: dto.title,
    district: dto.district,
    lat: dto.latitude,
    lng: dto.longitude,
    createdAt: dto.createdAt,
    aiDescription: dto.aiDescription,
    caption: dto.caption,
    aiConfidence: dto.aiConfidence,
    source: dto.source?.toLowerCase?.() ?? dto.source ?? "api",
    imageUrl: dto.imageUrl,
    status: dto.status,
  };
}
