import datetime
from src.utils.adapter_db import MongoDBAdapter
from src.utils.auth import hash_password, check_password, create_token
import uuid
from dotenv import load_dotenv
import os

load_dotenv() 
mongo_URI = os.getenv("mongo_URI")
db_name = os.getenv("db_name")
users_collection = os.getenv("users_collection")

class UsersService():   
    """
    User managing: registers new users, logging in, updates locations and returns available users.
    """ 
    def __init__(self):
        self.users_collection = MongoDBAdapter(mongo_URI, db_name, users_collection)

    def login_user(self, email: str, password: str):
        # Check if the email is already registered in the database
        user = self.users_collection.find_one({"email": email})
        if not user:
            return {"error": "User not found"}, 404          
        if(not check_password(password, user.get("password"))):
            return {"error": "Password does not match"}, 403
        # Generate and return token
        token = create_token(user.get("user_id"))
        return {"token": token}, 200

    def new_user(self, name: str, email: str, password: str):
        # Check if the email is already registered in the database
        user = self.users_collection.find_one({"email": email})
        if user:
            return {"Error": "E-mail is already registered"}, 400
        user_id = str(uuid.uuid4())
        new_user = {
            "user_id": user_id,
            "name": name,
            "email": email,
            "password": hash_password(password),
            "latitude": None,
            "longitude": None,
            "chats": {},
            "last_connection": datetime.datetime.now()
        }
        self.users_collection.insert_one(new_user)
        # Generate and return token
        token = create_token(user_id)
        return {"token": token}, 200

    def update_location(self, user_id: str, latitude: float, longitude: float):
        query = {"user_id": user_id}
        update_data = {
            "latitude": latitude,
            "longitude": longitude,
            "last_connection": datetime.datetime.now()
        }
        self.users_collection.update_one(query, {"$set": update_data})
        return True
    
    def get_available_users(self, user_id, map_radio):
        # Check user is active in database
        user = self.users_collection.find_one({"user_id":user_id})
        if not user:
            return {"Error": "User not found"}, 404   
        # Get available users
        # Approximate distances from meters to latitud/longitud (distance / 111320)
        max_angle = map_radio/111320
        # User online if max time without conexion 1 minute
        time_limit = datetime.datetime.now() - datetime.timedelta(minutes=1)
        lat = user.get("latitude")
        lng = user.get("longitude")
        min_lat = lat - max_angle
        max_lat = lat + max_angle
        min_lng = lng - max_angle
        max_lng = lng + max_angle
        query = {"latitude": {"$gte": min_lat, "$lte": max_lat},
            "longitude": {"$gte": min_lng, "$lte": max_lng},
            # Exclude users that are not connected
            "last_connection": {"$gte": time_limit},
            # Exclude the user sending the request
            "user_id": {"$ne": user_id}
        }
        return self.users_collection.find_many(query)