from enum import member

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session, joinedload
from starlette import status
from starlette.status import HTTP_403_FORBIDDEN, HTTP_404_NOT_FOUND

from database.base import get_db
from models.meetings import Meeting, MeetingMembers, MeetingLocation
from models.user import User
from schemas.meetings import MeetingListSchema, MeetingSchema, MeetingLocationSchema
from services.auth import get_current_user

router = APIRouter(
    prefix='/meetings'
)

@router.get('/', response_model=MeetingListSchema)
def get_meetings(user = Depends(get_current_user), session: Session = Depends(get_db)):
    meetings = (
        session.query(Meeting)
                .join(Meeting, MeetingMembers.meeting_id == Meeting.id)
                .options(joinedload(Meeting.location))
                .filter(MeetingMembers.member_id == user.id)
                .all()
    )
    return {
        "meetings":meetings,
        "count":len(meetings)
    }


@router.get('/{meeting_id}', response_model=MeetingSchema)
def get_meeting(meeting_id: int,
                user: User = Depends(get_current_user),
                session: Session = Depends(get_db)):
    # Query meeting ensuring user is a member
    meeting = (session.query(Meeting)
               .join(MeetingMembers)
               .filter(Meeting.id == meeting_id)
               .filter(MeetingMembers.member_id == user.id)
               .first())

    if not meeting:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND,
                            detail="Meeting not found or access denied!")

    return meeting

@router.get('/{meeting_id}/location', response_model=MeetingLocationSchema)
def get_meeting_location(meeting_id:int,
                user: User = Depends(get_current_user),
                session: Session = Depends(get_db)):
    membership = (session.query(MeetingMembers)
                  .filter(MeetingMembers.meeting_id == meeting_id)
                  .filter(MeetingMembers.member_id == user.id)
                  .first())
    if not membership:
        raise HTTPException(status_code=HTTP_403_FORBIDDEN,
                            detail="You can't access details if you are not part of the meeting!")

    location = session.query(MeetingLocation).filter(MeetingLocation.meeting_id == meeting_id).first()
    if not location:
        raise HTTPException(status_code=HTTP_404_NOT_FOUND,
                            detail="This meeting has no location!")
    return location

