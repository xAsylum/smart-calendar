from sqlalchemy import Column, String
from werkzeug.security import generate_password_hash, check_password_hash

from database.base import Base
from pydantic import BaseModel, Field


class User(Base):
    __tablename__ = 'user'
    username = Column(
        String(64),
        primary_key=True,
        unique= True
    )
    password_hash = Column(
        String(128),
        nullable=False
    )

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

class LoginRequest(BaseModel):
    username: str
    password: str

class RegisterRequest(BaseModel):
    username: str
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str