import json
import os
from typing import List, Dict
from pydantic import BaseModel


class ChatMessage(BaseModel):
    id: int
    sender_id: int
    sender_name: str
    message: str
    timestamp: str


class ChatStorage:
    def __init__(self, storage_dir: str = "chat_data"):
        self.storage_dir = storage_dir
        if not os.path.exists(storage_dir):
            os.makedirs(storage_dir)

    def _get_file_path(self, meeting_id: int) -> str:
        return os.path.join(self.storage_dir, f"meeting_{meeting_id}.json")

    def get_messages_after(self, meeting_id: int, last_id: int) -> List[dict]:
        file_path = self._get_file_path(meeting_id)
        if not os.path.exists(file_path):
            return []

        with open(file_path, "r", encoding="utf-8") as f:
            try:
                messages = json.load(f)
                return [msg for msg in messages if msg["id"] > last_id]
            except json.JSONDecodeError:
                return []

    def save_message(self, meeting_id: int, sender_id: int, sender_name: str, message: str) -> ChatMessage:
        file_path = self._get_file_path(meeting_id)
        messages = []

        if os.path.exists(file_path):
            with open(file_path, "r", encoding="utf-8") as f:
                try:
                    messages = json.load(f)
                except json.JSONDecodeError:
                    pass

        new_id = 1 if not messages else messages[-1]["id"] + 1

        import datetime
        now = datetime.datetime.utcnow().isoformat()

        new_msg = {
            "id": new_id,
            "sender_id": sender_id,
            "sender_name": sender_name,
            "message": message,
            "timestamp": now
        }

        messages.append(new_msg)

        with open(file_path, "w", encoding="utf-8") as f:
            json.dump(messages, f, ensure_ascii=False, indent=2)

        return ChatMessage(**new_msg)