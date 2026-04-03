from debugpy.adapter import access_token
from fastapi import APIRouter, HTTPException
from fastapi.params import Depends
from sqlalchemy.dialects.sqlite import insert
from sqlalchemy.orm import Session
from starlette import status

from database.base import get_db
from models.user import Token, LoginRequest, RegisterRequest, User
from services.auth import create_access_token, authenticate_user, get_user

router = APIRouter(
    prefix='/auth'
)

@router.post("/login")
async def login(login_data : LoginRequest, session: Session = Depends(get_db)):
    if not authenticate_user(login_data.username, login_data.password, session):
        raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid username of password!",
        headers={"WWW-Authenticate": "Bearer"},
        )
    access_token = create_access_token(login_data.username)
    return Token(access_token=access_token,token_type='bearer')


@router.post("/register")
async def register(register_data: RegisterRequest, session: Session = Depends(get_db)):
    if get_user(register_data.username, session):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"User {register_data.username} already exists!"
        )
    new_account: User = User(username=register_data.username)
    new_account.set_password(register_data.password)
    session.add(new_account)
    session.commit()
    access_token = create_access_token(register_data.username)
    return Token(access_token=access_token,token_type='bearer')