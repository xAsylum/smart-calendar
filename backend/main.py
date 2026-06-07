from dotenv import load_dotenv
from starlette.middleware.cors import CORSMiddleware

load_dotenv()

from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
from routers import auth, friends, meetings, chat, distance
from database.base import get_db, Base, engine

app = FastAPI()

origins = [
    "*"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
app.include_router(auth.router)
app.include_router(friends.router)
app.include_router(meetings.router)
app.include_router(chat.router)
app.include_router(distance.router)

Base.metadata.create_all(bind=engine)
@app.get("/test-db")
async def test_db(db: Session = Depends(get_db)):
    return {"message": "Database session work"}
