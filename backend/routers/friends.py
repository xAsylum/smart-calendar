
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from starlette import status

from database.base import get_db
from models.friends import Friend, FriendRequest
from models.user import User
from schemas.friends import FriendRequestSchema
from services.auth import get_current_user

router = APIRouter(
    prefix='/friends'
)

@router.get('/requests')
def get_friend_requests(user: User = Depends(get_current_user),
                        session: Session = Depends(get_db)):
    requests = (session.query(Friend, User)
                .join(User, Friend.friend == User.id)
                .filter(Friend.friend == user.id))

    return {"requests": [
        {
            "id" : rel.id,
            "username" : user.username
        }
        for (rel, user) in
        requests]
    }

@router.post('/')
def send_friend_request(request: FriendRequestSchema,
                        user: User = Depends(get_current_user),
                        session: Session = Depends(get_db)):
    username: str = request.username
    friend: User | None = session.query(User).filter_by(username = username).first()
    if not friend:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="User you try to invite doesn't exist!"
        )
    invite = session.query(FriendRequest).filter_by(request_from = user.id, request_to = friend.id).first()
    if invite:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invite already sent!"
        )
    invite = FriendRequest(request_from=user.id, request_to = friend.id)
    session.add(invite)
    session.commit()
    return {"message" : "Invite sent successfully!" }