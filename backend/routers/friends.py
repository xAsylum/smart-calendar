from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from database.base import get_db
from models.friends import Friend
from models.user import User
from services.auth import get_current_user

router = APIRouter(
    prefix='/friends'
)

@router.get('/requests')
def get_friend_requests(user: User = Depends(get_current_user) ,session: Session = Depends(get_db)):

    requests = session.query(Friend).filter_by(owner = user.username)
    return {"requests": [r.friend for r in requests]}

