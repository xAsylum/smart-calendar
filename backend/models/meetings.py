from sqlalchemy import Column, String, ForeignKey, Integer, Float, DateTime, Interval
from sqlalchemy.orm import relationship

from database.base import Base
from models.user import User

class Meeting(Base):
    __tablename__ = 'meetings'
    id = Column(Integer,
                primary_key=True, autoincrement=True)

    owner = Column(Integer,
                   ForeignKey(User.id),
                   nullable=False)

    name = Column(String(64),
                  nullable=False)

    start_time = Column(DateTime,
                        nullable=False)

    duration = Column(Interval,
                      nullable=False)

    location = relationship("MeetingLocation",
                            uselist=False,
                            back_populates="meeting")


class MeetingLocation(Base):
    __tablename__ = 'meetings_location'
    meeting_id = Column(Integer,
                        ForeignKey(Meeting.id),
                        primary_key=True)

    address = Column(String,
                     nullable=True)

    latitude = Column(Float,
                      nullable = True)

    longitude = Column(Float,
                       nullable=True)

    meeting = relationship("Meeting",
                           back_populates="location")



class MeetingMembers(Base):
    __tablename__ = 'meetings_members'
    meeting_id = Column(Integer,
                        ForeignKey(Meeting.id),
                        primary_key=True)

    member_id = Column(Integer,
                       ForeignKey(User.id),
                       primary_key=True)

