from pydantic import BaseModel

class FriendRequestSchema(BaseModel):
    username: str