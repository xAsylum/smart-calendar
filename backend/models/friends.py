from sqlalchemy import Column, String, ForeignKey, UniqueConstraint
from database.base import Base
from models.user import User


class Friend(Base):
    __tablename__ = 'friends'
    owner = Column(String,
                   ForeignKey(User.username),
                   nullable=False,
                   primary_key=True
    )
    friend = Column(String,
                    ForeignKey(User.username),
                    nullable=False,
                    primary_key=True
    )
    __table_args__ = (UniqueConstraint('owner', 'friend', name = 'uniq'),)


class FriendRequest(Base):
    __tablename__ = 'friend_requests'
    request_from = Column(String,
                   ForeignKey(User.username),
                   nullable=False,
                   primary_key=True
    )
    request_to = Column(String,
                    ForeignKey(User.username),
                    nullable=False,
                    primary_key=True
    )
    #__table_args__ = (UniqueConstraint('request_from', 'request_to', name = 'uniq'))