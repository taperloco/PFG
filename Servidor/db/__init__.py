import datetime
from pymongo import MongoClient
from dotenv import load_dotenv
import os

def create_db():
    """
    Mockup db for testing the
    """
    load_dotenv() 
    mongo_URI = os.getenv("mongo_URI")
    db_name = os.getenv("db_name")
    users_collection = os.getenv("users_collection")
    recados_collection = os.getenv("recados_collection")
    chats_collection = os.getenv("chats_collection")

    # Connect to MongoDB
    client = MongoClient(mongo_URI)

    # Select database and collections
    db = client[db_name]
    users_collection = db[users_collection]
    recados_collection = db[recados_collection]
    chats_collection = db[chats_collection]

    # Delete if there were old collections
    users_collection.drop()
    recados_collection.drop()
    chats_collection.drop()

    # Mock up data
    user_list = [
        {
            "user_id": "23a3639c-a649-40d5-91cb-1316fc558ed7",
            "name": "Juan",
            "email": "juan@gmail.com",
            "password": "$2b$12$jFb8Ntzcl4P.t4FVr6pfN.BnDCu3.MstzMr5rVQBQlIawM1swcQCS",
            "latitude": 36.7087493,
            "longitude": -4.444492,
            "chats": {},
            "last_connection": datetime.datetime.now()
        },
        {
            "user_id": "8d3a62eb-4962-4f44-b5c7-985c4d56e6ef",
            "name": "Maria",
            "email": "maria@gmail.com",
            "password": "$2b$12$jFb8Ntzcl4P.t4FVr6pfN.BnDCu3.MstzMr5rVQBQlIawM1swcQCS",
            "latitude": 36.7088153,
            "longitude": -4.444172,
            "chats": {},
            "last_connection": datetime.datetime.now()
        },
        {
            "user_id": "30a0f575-fa5c-4e10-8f25-e5cba6b05474",
            "name": "Carlos",
            "email": "carlos@gmail.com",
            "password": "1234",
            "latitude": 36.7086243,
            "longitude": -4.444457,
            "chats": {},
            "last_connection": datetime.datetime.now()
        },
        {
            "user_id": "28b2c8f3-1ff9-4dc7-a29a-a8140754a2c4",
            "name": "Ana",
            "email": "ana@gmail.com",
            "password": "1234",
            "latitude": 36.7084913,
            "longitude": -4.444451,
            "chats": {},
            "last_connection": datetime.datetime.now()
        },
        {
            "user_id": "dd203b00-e4bd-4875-97dc-522837ff6ee3",
            "name": "Pedro",
            "email": "pedro@gmail.com",
            "password": "1234",
            "latitude": 36.7085463,
            "longitude": -4.443936,
            "chats": {},
            "last_connection": datetime.datetime.now()
        }
    ]

    recado_list = [
        {
            "recado_id": "04ff0504-4633-45aa-88f8-0d64b7107877",
            "text": "Juan estuvo aqui.",
            "creator_id": "23a3639c-a649-40d5-91cb-1316fc558ed7",
            "creator_name": "Juan",
            "latitude": 36.7087493,
            "longitude": -4.444492,
            "timestamp": datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
        },
        {
            "recado_id": "b286248c-4de7-417a-9b46-f26d75e39921",
            "text": "Que fuente mas bonita. Soy Carlos",
            "creator_id": "30a0f575-fa5c-4e10-8f25-e5cba6b05474",
            "creator_name": "Carlos",
            "latitude": 36.7087433,
            "longitude": -4.444328,
            "timestamp": datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
        }
    ]

    # Insert data
    users_collection.insert_many(user_list)
    recados_collection.insert_many(recado_list)

    return db