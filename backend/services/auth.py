from datetime import datetime, timezone, timedelta

import jwt
from fastapi import Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer, HTTPBearer, HTTPAuthorizationCredentials
import os

from jwt import InvalidTokenError
from sqlalchemy.orm import Session
from starlette import status

from database.base import get_db
from models.user import User

security = HTTPBearer(bearerFormat='JWT')
secret = os.getenv('JWT_SECRET_KEY')
algorithm = os.getenv('JWT_ALGORITHM')

def create_access_token(username: str):
    expires_delta = datetime.now(timezone.utc) + timedelta(weeks=2)
    data = {
        "sub" : username,
        "exp" : expires_delta
    }
    encoded_jwt = jwt.encode(data, secret, algorithm=algorithm)
    return encoded_jwt

async def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security), session: Session = Depends(get_db)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(credentials.credentials, secret, algorithms=[algorithm])
        username = payload.get("sub")
        expires = payload.get("exp")
        if username is None:
            raise credentials_exception
    except InvalidTokenError:
        raise credentials_exception
    user = session.query(User).filter_by(username=username).first()
    if user is None:
        raise credentials_exception
    return user

def get_user(username: str, session: Session = Depends(get_db)):
    user = session.query(User).filter_by(username=username).first()
    return user

def authenticate_user(username:str, password:str, session: Session = Depends(get_db)):
    user = get_user(username, session)
    if not user or not user.check_password(password=password):
        return False
    return True