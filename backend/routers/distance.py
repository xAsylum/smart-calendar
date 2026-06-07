import os
import googlemaps
import httpx
from fastapi import Query, APIRouter, HTTPException

API_KEY = os.getenv("GOOGLE_API_KEY", "")
gmaps = googlemaps.Client(key=API_KEY)

router = APIRouter(
    prefix='/distance'
)

@router.get("/travel-time")
async def get_travel_time(
        origin_lat: float = Query(..., description="Origin latitude"),
        origin_lng: float = Query(..., description="Origin longitude"),
        dest_lat: float = Query(..., description="Destination latitude"),
        dest_lng: float = Query(..., description="Destination longitude"),
        mode: str = Query("driving", description="Mode: driving, walking, bicycling")
):
    url = "https://routes.googleapis.com/directions/v2:computeRoutes"

    # The new Routes API uses uppercase modes (DRIVE, WALK, BICYCLE)
    # We map your existing lowercase modes to keep Android compatibility
    mode_mapping = {
        "driving": "DRIVE",
        "walking": "WALK",
        "bicycling": "BICYCLE"
    }
    route_mode = mode_mapping.get(mode.lower(), "DRIVE")

    headers = {
        "Content-Type": "application/json",
        "X-Goog-Api-Key": API_KEY,
        "X-Goog-FieldMask": "routes.duration,routes.distanceMeters"
    }

    payload = {
        "origin": {
            "location": {"latLng": {"latitude": origin_lat, "longitude": origin_lng}}
        },
        "destination": {
            "location": {"latLng": {"latitude": dest_lat, "longitude": dest_lng}}
        },
        "travelMode": route_mode
    }
    if route_mode == "DRIVE":
        payload["routingPreference"] = "TRAFFIC_AWARE_OPTIMAL"

    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(url, json=payload, headers=headers)

            if response.status_code != 200:
                raise HTTPException(status_code=response.status_code, detail=response.text)

            data = response.json()

            if "routes" in data and len(data["routes"]) > 0:
                route = data["routes"][0]

                duration_str = route.get("duration", "0s")
                duration_seconds = int(duration_str.replace("s", ""))
                distance_meters = route.get("distanceMeters", 0)

                duration_minutes = duration_seconds // 60
                duration_text = f"{duration_minutes} mins"
                distance_text = f"{distance_meters / 1000:.1f} km"

                return {
                    "duration_text": duration_text,
                    "duration_seconds": duration_seconds,
                    "distance_text": distance_text,
                    "mode": mode
                }
            else:
                raise HTTPException(status_code=400, detail="ZERO_RESULTS: No route could be found.")

        except Exception as e:
            raise HTTPException(status_code=500, detail=str(e))