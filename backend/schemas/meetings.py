from datetime import datetime, timedelta
from typing import List

from pydantic import BaseModel, ConfigDict
from typing import Optional


class MeetingLocationSchema(BaseModel):
    meeting_id: int
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    address: Optional[str] = None

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

class MeetingUpdateSchema(BaseModel):
    name: Optional[str] = None
    start_time: Optional[datetime] = None
    duration: Optional[timedelta] = None

    address: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None