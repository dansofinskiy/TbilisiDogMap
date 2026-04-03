import { loadPhotoDetails, loadPhotoMarkers } from "./api.js";

const tbilisiCenter = [44.8015, 41.7151];
const languageSelect = document.querySelector("#language-select");
const mapStatus = document.querySelector("#map-status");
const translatableElements = document.querySelectorAll("[data-i18n]");
const modalRoot = document.querySelector("#modal-root");
const modalContent = document.querySelector("#modal-content");

const translations = {
  ru: {
    htmlLang: "ru",
    titleText: "Tbilisi Dog Map",
    eyebrow: "dog atlas",
    title: "Карта собак Тбилиси",
    subtitle:
      "Геометки с фотографиями собак и AI-описаниями. Карточка открывается прямо на карте.",
    botLabel: "Отправить фото в бота:",
    languageLabel: "Язык приложения",
    popupEyebrow: "выбранная точка",
    confidence: "AI confidence",
    coordinates: "Координаты",
    addedAt: "Добавлено",
    popupLoading: "Загружаем карточку фотографии...",
    popupError: "Не удалось загрузить карточку фотографии.",
    statusLoading: "Загружаем точки с сервера...",
    statusApi: "Геометки загружены с Kotlin API.",
    statusEmpty: "Пока нет опубликованных фотографий.",
    statusError: "Не удалось загрузить точки с сервера.",
    clusterEyebrow: "группа фотографий",
    clusterPhotos: "фотографий в этой зоне",
    clusterAction: "Приблизить карту",
    dateLocale: "ru-RU",
  },
  en: {
    htmlLang: "en",
    titleText: "Tbilisi Dog Map",
    eyebrow: "dog atlas",
    title: "Tbilisi Dog Map",
    subtitle:
      "Geotagged dog photos with AI descriptions. Each card opens directly on the map.",
    botLabel: "Send a photo to the bot:",
    languageLabel: "Application language",
    popupEyebrow: "selected point",
    confidence: "AI confidence",
    coordinates: "Coordinates",
    addedAt: "Added",
    popupLoading: "Loading photo details...",
    popupError: "Could not load photo details.",
    statusLoading: "Loading map points from the server...",
    statusApi: "Markers loaded from the Kotlin API.",
    statusEmpty: "There are no published photos yet.",
    statusError: "Could not load map points from the server.",
    clusterEyebrow: "photo cluster",
    clusterPhotos: "photos in this area",
    clusterAction: "Zoom in",
    dateLocale: "en-US",
  },
  ka: {
    htmlLang: "ka",
    titleText: "თბილისის ძაღლების რუკა",
    eyebrow: "dog atlas",
    title: "თბილისის ძაღლების რუკა",
    subtitle:
      "ძაღლების ფოტოები გეომონიშნებით და AI აღწერებით. ბარათი პირდაპირ რუკაზე იხსნება.",
    botLabel: "ფოტოს გაგზავნა ბოტში:",
    languageLabel: "აპლიკაციის ენა",
    popupEyebrow: "არჩეული წერტილი",
    confidence: "AI სანდოობა",
    coordinates: "კოორდინატები",
    addedAt: "დამატების თარიღი",
    popupLoading: "ფოტოს ბარათი იტვირთება...",
    popupError: "ფოტოს ბარათის ჩატვირთვა ვერ მოხერხდა.",
    statusLoading: "რუკის წერტილები იტვირთება სერვერიდან...",
    statusApi: "გეომონიშნებები ჩაიტვირთა Kotlin API-დან.",
    statusEmpty: "გამოქვეყნებული ფოტოები ჯერ არ არის.",
    statusError: "სერვერიდან წერტილების ჩატვირთვა ვერ მოხერხდა.",
    clusterEyebrow: "ფოტოების ჯგუფი",
    clusterPhotos: "ფოტო ამ არეალში",
    clusterAction: "რუკის მიახლოება",
    dateLocale: "ka-GE",
  },
};

let currentLanguage = languageSelect?.value || "ru";
let markers = [];
let lastLoadState = "loading";
const photoCache = new Map();
let hasLoadedMarkers = false;

const map = new maplibregl.Map({
  container: "map",
  style: {
    version: 8,
    sources: {
      osm: {
        type: "raster",
        tiles: ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
        tileSize: 256,
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      },
    },
    layers: [{ id: "osm", type: "raster", source: "osm" }],
  },
  center: tbilisiCenter,
  zoom: 12,
});

map.addControl(new maplibregl.NavigationControl(), "top-right");

map.on("load", async () => {
  applyTranslations();
  setStatus("statusLoading");

  renderClusters();
  bindMapInteractions();

  try {
    await refreshMarkersForViewport();
    map.on("moveend", () => {
      void refreshMarkersForViewport();
    });
  } catch (error) {
    console.error("Failed to load map markers:", error);
    markers = [];
    lastLoadState = "error";
    updateMarkerSource();
    setStatus("statusError");
  }
});

function renderClusters() {
  map.addSource("dog-photos", {
    type: "geojson",
    data: createFeatureCollection(markers),
    cluster: true,
    clusterMaxZoom: 14,
    clusterRadius: 48,
  });

  map.addLayer({
    id: "clusters-halo",
    type: "circle",
    source: "dog-photos",
    filter: ["has", "point_count"],
    paint: {
      "circle-color": "rgba(223, 108, 47, 0.20)",
      "circle-radius": ["step", ["get", "point_count"], 26, 10, 34, 25, 42],
      "circle-stroke-width": 0,
    },
  });

  map.addLayer({
    id: "clusters",
    type: "circle",
    source: "dog-photos",
    filter: ["has", "point_count"],
    paint: {
      "circle-color": [
        "step",
        ["get", "point_count"],
        "#df6c2f",
        10,
        "#cc5b21",
        25,
        "#a93f14",
      ],
      "circle-radius": ["step", ["get", "point_count"], 19, 10, 25, 25, 31],
      "circle-stroke-width": 4,
      "circle-stroke-color": "rgba(255, 248, 240, 0.98)",
      "circle-opacity": 0.96,
    },
  });

  map.addLayer({
    id: "cluster-count",
    type: "symbol",
    source: "dog-photos",
    filter: ["has", "point_count"],
    layout: {
      "text-field": ["get", "point_count_abbreviated"],
      "text-font": ["Noto Sans Regular"],
      "text-size": 14,
    },
    paint: {
      "text-color": "#fffaf0",
    },
  });

  map.addLayer({
    id: "unclustered-point",
    type: "circle",
    source: "dog-photos",
    filter: ["!", ["has", "point_count"]],
    paint: {
      "circle-color": "#df6c2f",
      "circle-radius": 9,
      "circle-stroke-width": 3,
      "circle-stroke-color": "rgba(255, 250, 244, 0.92)",
    },
  });
}

async function refreshMarkersForViewport() {
  setStatus("statusLoading");
  markers = await loadPhotoMarkers(map.getBounds());
  lastLoadState = "api";
  updateMarkerSource();
  hasLoadedMarkers = true;

  if (markers.length === 0) {
    setStatus("statusEmpty");
    return;
  }

  setStatus("statusApi");
}

function updateMarkerSource() {
  map.getSource("dog-photos")?.setData(createFeatureCollection(markers));
}

function bindMapInteractions() {
  map.on("click", "clusters", (event) => {
    const feature = map.queryRenderedFeatures(event.point, {
      layers: ["clusters"],
    })[0];

    if (!feature) {
      return;
    }

    const clusterId = feature.properties.cluster_id;
    const pointCount = feature.properties.point_count;

    map.getSource("dog-photos").getClusterExpansionZoom(clusterId, (error, zoom) => {
      if (error) {
        return;
      }

      openModal(
        createClusterPopupElement({
          pointCount,
          onAction: () => {
            closeModal();
            map.easeTo({
              center: feature.geometry.coordinates,
              zoom,
              duration: 500,
            });
          },
        }),
      );
    });
  });

  map.on("click", "unclustered-point", async (event) => {
    const feature = event.features?.[0];

    if (!feature) {
      return;
    }

    const photoId = feature.properties.id;
    const marker = markers.find((entry) => entry.id === photoId);

    if (!marker) {
      return;
    }

    openModal(createLoadingPopupMarkup());

    try {
      const photo = await getPhotoDetails(photoId);
      openModal(createPopupMarkup(photo));
    } catch (error) {
      console.error(`Failed to load photo ${photoId}:`, error);
      openModal(createErrorPopupMarkup());
    }
  });

  map.on("mouseenter", "clusters", () => {
    map.getCanvas().style.cursor = "pointer";
  });

  map.on("mouseleave", "clusters", () => {
    map.getCanvas().style.cursor = "";
  });

  map.on("mouseenter", "unclustered-point", () => {
    map.getCanvas().style.cursor = "pointer";
  });

  map.on("mouseleave", "unclustered-point", () => {
    map.getCanvas().style.cursor = "";
  });
}

async function getPhotoDetails(photoId) {
  if (photoCache.has(photoId)) {
    return photoCache.get(photoId);
  }

  const photo = await loadPhotoDetails(photoId);
  photoCache.set(photoId, photo);
  return photo;
}

function createLoadingPopupMarkup() {
  return `<article class="popup-card popup-state"><p>${translations[currentLanguage].popupLoading}</p></article>`;
}

function createErrorPopupMarkup() {
  return `<article class="popup-card popup-state"><p>${translations[currentLanguage].popupError}</p></article>`;
}

function createPopupMarkup(photo) {
  const t = translations[currentLanguage];

  return `
    <article class="popup-card">
      <img class="details-image" src="${photo.imageUrl}" alt="${photo.title}" />
      <div class="details-header">
        <div>
          <p class="eyebrow">${t.popupEyebrow}</p>
          <h3>${photo.title}</h3>
        </div>
        <span class="status-pill">${photo.source}</span>
      </div>
      <p class="details-subtitle">${photo.aiDescription}</p>
      <p class="details-caption">${photo.caption}</p>
      <div class="details-grid">
        <div class="details-stat">
          <span class="details-stat-label">${t.confidence}</span>
          <span class="details-stat-value">${Math.round(photo.aiConfidence * 100)}%</span>
        </div>
        <div class="details-stat">
          <span class="details-stat-label">${t.coordinates}</span>
          <span class="details-stat-value">${photo.lat.toFixed(4)}, ${photo.lng.toFixed(4)}</span>
        </div>
        <div class="details-stat">
          <span class="details-stat-label">${t.addedAt}</span>
          <span class="details-stat-value">${formatDate(photo.createdAt)}</span>
        </div>
      </div>
    </article>
  `;
}

function createClusterPopupElement({ pointCount, onAction }) {
  const t = translations[currentLanguage];
  const article = document.createElement("article");
  article.className = "popup-card cluster-card";
  article.innerHTML = `
    <div class="cluster-card-body">
      <p class="eyebrow">${t.clusterEyebrow}</p>
      <div class="cluster-badge">${pointCount}</div>
      <p class="cluster-summary">${t.clusterPhotos}</p>
      <button class="cluster-action" type="button">${t.clusterAction}</button>
    </div>
  `;

  article.querySelector(".cluster-action")?.addEventListener("click", onAction);
  return article;
}

function openModal(content) {
  if (!modalRoot || !modalContent) {
    return;
  }

  if (typeof content === "string") {
    modalContent.innerHTML = content;
  } else {
    modalContent.replaceChildren(content);
  }

  modalRoot.hidden = false;
  document.body.style.overflow = "hidden";
}

function closeModal() {
  if (!modalRoot || !modalContent) {
    return;
  }

  modalRoot.hidden = true;
  modalContent.innerHTML = "";
  document.body.style.overflow = "";
}

function createFeatureCollection(items) {
  return {
    type: "FeatureCollection",
    features: items.map((photo) => ({
      type: "Feature",
      geometry: {
        type: "Point",
        coordinates: [photo.lng, photo.lat],
      },
      properties: {
        id: photo.id,
      },
    })),
  };
}

function applyTranslations() {
  const t = translations[currentLanguage];

  document.documentElement.lang = t.htmlLang;
  document.title = t.titleText;
  languageSelect?.setAttribute("aria-label", t.languageLabel);

  translatableElements.forEach((element) => {
    const key = element.dataset.i18n;

    if (t[key]) {
      element.textContent = t[key];
    }
  });

  if (!modalRoot?.hidden) {
    closeModal();
  }
}

function updateMapFocus() {
  if (hasLoadedMarkers) {
    return;
  }

  if (markers.length === 0) {
    map.flyTo({ center: tbilisiCenter, zoom: 11.5, essential: true });
    return;
  }

  if (markers.length === 1) {
    const [photo] = markers;
    map.flyTo({ center: [photo.lng, photo.lat], zoom: 14, essential: true });
    return;
  }

  const bounds = new maplibregl.LngLatBounds();
  markers.forEach((photo) => bounds.extend([photo.lng, photo.lat]));
  map.fitBounds(bounds, { padding: 64, maxZoom: 14, duration: 800 });
}

function formatDate(value) {
  return new Intl.DateTimeFormat(translations[currentLanguage].dateLocale, {
    day: "2-digit",
    month: "long",
    year: "numeric",
  }).format(new Date(value));
}

function setStatus(key) {
  if (!mapStatus) {
    return;
  }

  mapStatus.textContent = translations[currentLanguage][key] ?? "";
}

languageSelect?.addEventListener("change", (event) => {
  currentLanguage = event.target.value;
  applyTranslations();

  if (markers.length === 0) {
    setStatus(lastLoadState === "error" ? "statusError" : "statusEmpty");
    return;
  }

  setStatus("statusApi");
});

modalRoot?.addEventListener("click", (event) => {
  const target = event.target;

  if (target instanceof HTMLElement && target.dataset.closeModal === "true") {
    closeModal();
  }
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape" && !modalRoot?.hidden) {
    closeModal();
  }
});
