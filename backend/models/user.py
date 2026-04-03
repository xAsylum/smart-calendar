from sqlalchemy import Column, String, Integer
from werkzeug.security import generate_password_hash, check_password_hash

from database.base import Base

class User(Base):
    __tablename__ = 'user'
    id = Column(Integer, primary_key=True, autoincrement=True)
    username = Column(
        String(64),
        unique= True)

    password_hash = Column(
        String(128),
        nullable=False)

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)
