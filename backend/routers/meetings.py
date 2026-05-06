import json
from enum import member
from typing import List

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session, joinedload
from starlette import status
from starlette.status import HTTP_403_FORBIDDEN, HTTP_404_NOT_FOUND, HTTP_400_BAD_REQUEST

from database.base import get_db
from models.meetings import Meeting, MeetingMembers, MeetingLocation
from models.user import User
from schemas.meetings import MeetingListSchema, MeetingSchema, MeetingLocationSchema, MeetingUpdateSchema
from services.auth import get_current_user

router = APIRouter(
    prefix='/meetings'
)

@router.get('', response_model=MeetingListSchema)
def get_meetings(user = Depends(get_current_user), session: Session = Depends(get_db)):
    meetings = (
        session.query(Meeting)
                .join(MeetingMembers, MeetingMembers.meeting_id == Meeting.id)
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

@router.post('', response_model=MeetingSchema)
def create_meeting(meeting_data : MeetingUpdateSchema,
                   user: User = Depends(get_current_user),
                   session: Session = Depends(get_db)):
    update = meeting_data.model_dump(exclude_unset=True)
    mandatory_fields = ['name', 'start_time', 'duration']
    for field in mandatory_fields:
        if field not in update:
            raise HTTPException(status_code=HTTP_400_BAD_REQUEST,
                                detail="Missing mandatory info!")
    meeting = Meeting(name=update['name'],
                      owner=user.id,
                      start_time=update['start_time'],
                      duration=update['duration'])


    session.add(meeting)
    session.commit()
    session.refresh(meeting)
    meeting_member = MeetingMembers(meeting_id=meeting.id, member_id = user.id)
    session.add(meeting_member)
    session.commit()
    return meeting

@router.put('/{meeting_id}', response_model=MeetingSchema)
def update_meeting(meeting_id: int,
                   meeting_data : MeetingUpdateSchema,
                   user: User = Depends(get_current_user),
                   session: Session = Depends(get_db)):
    meeting = (session.query(Meeting)
                  .filter_by(id=meeting_id)
                  .first())
    if not meeting or meeting.owner != user.id:
        raise HTTPException(status_code=HTTP_403_FORBIDDEN,
                            detail="You can't edit the meeting you are not the owner of!")

    update=meeting_data.model_dump(exclude_unset=True)

    location_keys = ['latitude', 'longitude', 'address']
    non_location_keys = ['name', 'start_time', 'duration']

    for key, value in update.items():
        if key in non_location_keys:
            setattr(meeting, key, value)

    if any(key in update for key in location_keys):
        location = session.query(MeetingLocation).filter_by(meeting_id=meeting_id).first()

        if not location:
            location = MeetingLocation(meeting_id=meeting_id)
            session.add(location)

        for key, value in update.items():
            if key in location_keys:
                setattr(location, key, value)

    session.commit()
    session.refresh(meeting)
    return meeting


@router.post('/{meeting_id}/members')
def update_meeting_members(meeting_id: int,
                           member_ids: List[int],
                           user: User = Depends(get_current_user),
                           session: Session = Depends(get_db)):
    meeting = session.query(Meeting).filter_by(id=meeting_id).first()
    if not meeting or meeting.owner != user.id:
        raise HTTPException(status_code=403, detail="Only owner can manage members")

    session.query(MeetingMembers).filter_by(meeting_id=meeting_id).delete()

    if user.id not in member_ids:
        member_ids.append(user.id)

    for m_id in member_ids:
        new_member = MeetingMembers(meeting_id=meeting_id, member_id=m_id)
        session.add(new_member)

    session.commit()
    return {"message": "Members updated successfully"}


@router.get('/{meeting_id}/members')
def get_meeting_members(meeting_id: int,
                           session: Session = Depends(get_db)):
    members = (
        session.query(MeetingMembers, User)
        .join(User, MeetingMembers.member_id == User.id)
        .filter(MeetingMembers.meeting_id == meeting_id)
        .all()
    )

    return  {"members": [{"user_id": mm.member_id, "username": user.username} for mm, user in members], "count": len(members) }