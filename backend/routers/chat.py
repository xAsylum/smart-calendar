from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Depends, Query, HTTPException
from sqlalchemy.orm import Session
import json

from database.base import get_db
from models.meetings import MeetingMembers, User
from services.connection_manager import chat_storage
from services.auth import verify_token

from services.connection_manager import  manager

router = APIRouter()


@router.websocket("/ws/chat/{meeting_id}")
async def websocket_chat(
        websocket: WebSocket,
        meeting_id: int,
        token: str = Query(...),
        db: Session = Depends(get_db)
):
    user = verify_token(token, db)
    if not user:
        await websocket.close(code=1008)
        return

    is_member = db.query(MeetingMembers).filter_by(
        meeting_id=meeting_id, member_id=user.id
    ).first()

    if not is_member:
        await websocket.close(code=1008)
        return

    await manager.connect(websocket, meeting_id)

    try:
        while True:
            data = await websocket.receive_text()
            payload = json.loads(data)
            action = payload.get("action")

            if action == "fetch_history":
                last_id = int(payload.get("last_id", 0))
                missed_messages = chat_storage.get_messages_after(meeting_id, last_id)

                await websocket.send_json({
                    "type": "history",
                    "messages": missed_messages
                })

            elif action == "send_message":
                text = payload.get("message", "").strip()
                if text:
                    new_msg = chat_storage.save_message(
                        meeting_id=meeting_id,
                        sender_id=user.id,
                        sender_name=user.username,
                        message=text
                    )

                    await manager.broadcast_to_meeting(meeting_id, {
                        "type": "new_message",
                        "message": new_msg.dict()
                    })

    except WebSocketDisconnect:
        manager.disconnect(websocket, meeting_id)
    except Exception as e:
        manager.disconnect(websocket, meeting_id)