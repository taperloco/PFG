from flask import Flask
from flask_restful import Api
from src.controllers.controller import Controller


def create_app():
    """
    Creates (Flask) app and (Flask_resftul) api
    """
    app = Flask(__name__)
    api = Api(app)
    
    # Endpoints
    api.add_resource(Controller, 
                     "/recado/getchat",
                     "/recado/recados",
                     "/recado/chats",                  
                     "/recado/login",
                     "/recado/register",
                     "/recado/token",
                     "/recado/update" 
                     )
    return app




