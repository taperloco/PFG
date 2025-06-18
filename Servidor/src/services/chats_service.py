from src.utils.adapter_db import MongoDBAdapter
import datetime
from bson import ObjectId
from dotenv import load_dotenv
import os

load_dotenv() 
mongo_URI = os.getenv("mongo_URI")
db_name = os.getenv("db_name")
users_collection = os.getenv("users_collection")
chats_collection = os.getenv("chats_collection")

class ChatsService():   
    """
    Handles chats: creates new messages and retries the conversations between users
    """ 
    def __init__(self):
        self.users_collection = MongoDBAdapter(mongo_URI, db_name, users_collection)
        self.chats_collection = MongoDBAdapter(mongo_URI, db_name, chats_collection)

    def new_chat_message(self, user_id: str, text: str, send_to: str):
        # Check if both users are registered
        user = self.users_collection.find_one({"user_id": user_id})
        recipient =  self.users_collection.find_one({"user_id": send_to})
        if not user or not recipient:
            return {"Error": "User not found"}, 404
        
        chat_id = user.get("chats").get(send_to) 
        if chat_id is None:
            # Create new chat
            new_chat = {
                "creator_id": user_id,
                "reciever_id": send_to,
                "messages" : []
            }
            chat_id = self.chats_collection.insert_one(new_chat)
            # Add new chat to both users chat dictionaries
            self.users_collection.update_one(
                {"user_id": user_id},
                {"$set": {f"chats.{send_to}": chat_id}}
            )
            self.users_collection.update_one(
                {"user_id": send_to},
                {"$set": {f"chats.{user_id}": chat_id}}
            )
            
        #Add message to the chat
        chat =  self.chats_collection.update_one(
            {"_id": ObjectId(chat_id)}, 
            {
                "$push": {
                    "messages": {
                        "Name": user.get("name"),
                        "Text": text,
                        "Date": datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
                    }
                }
            }
        )
        return True

    def get_chat_messages(self, user_id: str, send_to: str):
        # Check if user is registered
        user = self.users_collection.find_one({"user_id": user_id})
        if not user:
            return {"Error": "User not found"}, 404
        
        chat_id = user.get("chats").get(send_to)
        if chat_id is None:
            return "Empty"
        else:
            # Return last 15 messages of the chat
            return self.chats_collection.find_one({"_id": ObjectId(chat_id)}).get("messages")[-15:]
        