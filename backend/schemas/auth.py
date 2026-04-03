from pydantic import BaseModel

class LoginRequestSchema(BaseModel):
    username: str
    password: str

class RegisterRequestSchema(BaseModel):
    username: str
    password: str

class TokenSchema(BaseModel):
    access_token: str
    token_type: str