
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from starlette import status

from database.base import get_db
from models.friends import Friend, FriendRequest
from models.user import User
from schemas.friends import FriendRequestSchema, FriendListSchema
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
        raise HTTPException(status_code=400, detail="User doesn't exist!")
    if friend.id == user.id:
        raise HTTPException(status_code=400, detail="You can't invite yourself.")
    already_friends = session.query(Friend).filter_by(owner=user.id, friend=friend.id).first()
    if already_friends:
        raise HTTPException(status_code=400, detail="You are already friends!")

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


@router.delete('/requests/{friend_id}')
def cancel_friend_request(friend_id: int,
                          user: User = Depends(get_current_user),
                          session: Session = Depends(get_db)):
    request = (session.query(FriendRequest)
               .filter_by(request_from=user.id, request_to=friend_id)
               .first())

    if not request:
        raise HTTPException(status_code=404, detail="Request not found.")

    session.delete(request)
    session.commit()
    return {"message": "Request cancelled successfully."}


@router.get('/requests/sent')
def get_sent_requests(user: User = Depends(get_current_user), session: Session = Depends(get_db)):
    requests = (session.query(FriendRequest, User)
                .join(User, FriendRequest.request_to == User.id)
                .filter(FriendRequest.request_from == user.id)
                .all())
    return {"requests": [{"id": rel.request_to, "username": receiver.username} for (rel, receiver) in requests]}

@router.post('/requests/{sender_id}/accept')
def accept_request_endpoint(sender_id: int, user: User = Depends(get_current_user), session: Session = Depends(get_db)):
    return accept_friend_request(sender_id, user, session)

@router.post('/requests/{sender_id}/reject')
def reject_friend_request(sender_id: int, user: User = Depends(get_current_user), session: Session = Depends(get_db)):
    db_request = (session.query(FriendRequest)
                  .filter_by(request_from=sender_id, request_to=user.id)
                  .first())
    if not db_request:
        raise HTTPException(status_code=404, detail="Request not found")
    session.delete(db_request)
    session.commit()
    return {"message": "Request rejected"}

@router.get('', response_model=FriendListSchema)
def get_friends(user: User = Depends(get_current_user),
                session: Session = Depends(get_db)):
    # Join the Friend table with User table to get the actual friend details
    friends = (session.query(User)
               .join(Friend, Friend.friend == User.id)
               .filter(Friend.owner == user.id)
               .all())

    return {
        "friends": friends,
        "count": len(friends)
    }