
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
    requests = (session.query(FriendRequest, User)
                .join(User, FriendRequest.request_from == User.id)
                .filter(FriendRequest.request_to == user.id)
                .all())

    return {"requests": [
        {
            "id" : rel.request_from,
            "username" : sender.username
        }
        for (rel, sender) in
        requests]
    }


def accept_friend_request(request_from: int,
                          user: User,
                          session: Session):
    db_request : FriendRequest | None = (session.query(FriendRequest)
                  .filter_by(request_from=request_from)
                  .filter_by(request_to=user.id)
                  .first())

    if not db_request:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Friend request not found or already processed."
        )

    new_friendship_1 = Friend(owner=db_request.request_from, friend=db_request.request_to)
    new_friendship_2 = Friend(owner=db_request.request_to, friend=db_request.request_from)

    session.add(new_friendship_1)
    session.add(new_friendship_2)

    session.delete(db_request)
    session.commit()
    return {"message": "Friend request accepted! You are now friends."}

@router.post('/requests')
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

    invite_back = session.query(FriendRequest).filter_by(request_from = friend.id, request_to = user.id).first()
    if invite_back:
        return accept_friend_request(friend.id, user, session)
    invite = FriendRequest(request_from=user.id, request_to = friend.id)
    session.add(invite)
    session.commit()
    return {"message" : "Invite sent successfully!" }