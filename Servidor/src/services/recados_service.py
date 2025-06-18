import datetime
from src.utils.adapter_db import MongoDBAdapter
import uuid
from dotenv import load_dotenv
import os

load_dotenv() 
mongo_URI = os.getenv("mongo_URI")
db_name = os.getenv("db_name")
recados_collection = os.getenv("recados_collection")
users_collection = os.getenv("users_collection")

class RecadosService():    
    """
    Manages recados: allowes users to create new ones and return a list of recados available
    """
    def __init__(self):
        self.recados_collection = MongoDBAdapter(mongo_URI, db_name, recados_collection)
        self.users_collection = MongoDBAdapter(mongo_URI, db_name, users_collection)

    def new_recado(self, user_id: str, datos: dict):
        # Check user is active in database
        user = self.users_collection.find_one({"user_id":user_id})
        if not user:
            return {"Error": "User not found"}, 400
        # Add user_id to the data
        datos["creator_id"] = user_id
        datos["creator_name"] = user.get("name")
        # Create a UUID and add it to the new recado
        datos["recado_id"] = str(uuid.uuid4())
        datos["timestamp"] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
        # Insert the new recado
        self.recados_collection.insert_one(datos)
        return True

    def get_available_recados(self, user_id, map_radio):
        # Check user is active in database
        user = self.users_collection.find_one({"user_id":user_id})
        if not user:
            return {"Error": "User not found"}, 400
        # Get available recados
        # Approximate distances from meters to latitud/longitud (distance / 111320)
        max_angle = map_radio/111320
        lat = user.get("latitude")
        lng = user.get("longitude")
        min_lat = lat - max_angle
        max_lat = lat + max_angle
        min_lng = lng - max_angle
        max_lng = lng + max_angle
        query = {
            "latitude": {"$gte": min_lat, "$lte": max_lat},
            "longitude": {"$gte": min_lng, "$lte": max_lng},
        }
        return self.recados_collection.find_many(query)   