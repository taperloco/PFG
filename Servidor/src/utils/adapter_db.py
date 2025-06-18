from pymongo import MongoClient
from pymongo.errors import ConnectionFailure

class MongoDBAdapter:
    """
    Interact with database. Services use it to CRUD information.
    """
    def __init__(self, mongo_URI, db_name, collection_name):
        self.uri = mongo_URI
        self.db_name = db_name
        self.collection_name = collection_name
        self._connect()

    def _connect(self):
        try:
            self.client = MongoClient(self.uri, serverSelectionTimeoutMS=5000)
            # Next line checks that the connection to the MongoDB server is actually working
            self.client.server_info() 
            db = self.client[self.db_name]
            self.collection = db[self.collection_name]
        except ConnectionFailure as e:
            raise

    def insert_one(self, data):
        result = self.collection.insert_one(data)
        return str(result.inserted_id)

    def find_one(self, query):
        return self.collection.find_one(query)
       
    def find_many(self, query):
        if self.collection_name == "Users_collection":
            # Do not extract fields that are not needed in the client
            return list(self.collection
                        .find(query, {"_id": 0, "email": 0, "password": 0, "chats": 0, "last_connection": 0})
                        .limit(20))
        else:
            # Do not extract fields that are not needed in the client (mongoID)
            return list(self.collection
                        .find(query, {"_id": 0})
                        .limit(20))

    def update_one(self, query, update_data):
        result = self.collection.update_one(query, update_data)
        return result.modified_count