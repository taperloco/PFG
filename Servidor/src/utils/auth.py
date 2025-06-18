from datetime import datetime, timedelta, timezone
import jwt
import bcrypt
import re

# For environmental variables
from dotenv import load_dotenv
import os

#Load secret keys from .env
load_dotenv() 
secret_key = os.getenv("CLAVE_SECRETA")

algorithm = "HS256"
# Expires in 1440 minutes = one day
token_expiry_minutes = 1440

def check_token(token):
    try:
        payload = jwt.decode(token, secret_key, algorithms=[algorithm])
        return payload.get("user_id"), "OK"
    except jwt.ExpiredSignatureError:
        return None, "Token expired"
    except jwt.InvalidTokenError:
        return None, "Invalid token"

def refresh_token(token):
    now = datetime.now(timezone.utc)
    payload = {
        "user_id": jwt.decode(token, secret_key, algorithms=[algorithm], options={"verify_exp": False}).get("user_id"),
        "exp": now + timedelta(minutes=token_expiry_minutes),
        "iat": now
    }
    token = jwt.encode(payload, secret_key, algorithm=algorithm)
    return token  
        
def create_token(user_id):
    now = datetime.now(timezone.utc)
    payload = {
        "user_id": user_id,
        "exp": now + timedelta(minutes=token_expiry_minutes),
        "iat": now
    }
    token = jwt.encode(payload, secret_key, algorithm=algorithm)
    return token

def hash_password(plain_password: str):
    hashed = bcrypt.hashpw(plain_password.encode("utf-8"), bcrypt.gensalt())
    return hashed.decode("utf-8")

def check_password(plain_password: str, hashed_password: str):
    return bcrypt.checkpw(plain_password.encode("utf-8"), hashed_password.encode("utf-8"))

def is_strong_password(password: str):
    if len(password) < 8:
        return False
    if not re.search(r"[A-Z]", password):      
        return False
    if not re.search(r"[a-z]", password):      
        return False
    if not re.search(r"\d", password):        
        return False
    if not re.search(r"[!@#$%^&*(),.?\":{}|<>]", password):  
        return False
    return True