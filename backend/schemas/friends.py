from typing import List

from pydantic import BaseModel, ConfigDict


class FriendRequestSchema(BaseModel):
    username: str


class FriendSchema(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    username: str

class FriendListSchema(BaseModel):
    friends: List[FriendSchema]
    count: int