from datetime import datetime, timedelta
from typing import List

from pydantic import BaseModel, ConfigDict


class MeetingLocationSchema(BaseModel):
    meeting_id: int
    latitude: float
    longitude: float
    address: str

class MeetingSchema(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: int
    owner: int
    name: str
    start_time: datetime
    duration: timedelta
    location: MeetingLocationSchema | None = None

class MeetingListSchema(BaseModel):
    meetings: List[MeetingSchema]
    count: int