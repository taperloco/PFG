from flask import request
from flask_restful import Resource
from src.services.recados_service import RecadosService
from src.services.users_service import UsersService
from src.services.chats_service import ChatsService
from src.utils.auth import *
from src.utils.helper_functions import *

class Controller(Resource):
    """
    Handles end points
    get:
    "/recado/getchat"
     put:
    "/recado/recados"
    "/recado/chats"
    post:                
    "/recado/login"
    "/recado/register"
    "/recado/token"
    "/recado/update"
    and dispatch them to the correct service.
    """
    def get(self):
        """
        Get chat messages
        """
        # Check auth
        auth_header = request.headers.get("Authorization")
        if auth_header and auth_header.startswith("Bearer "):
            token = auth_header.split(" ")[1]
        else:
            return {"error": "Missing authorization header"}, 401
        # Check that jwt token is correct and extract the user.
        user_id, error = check_token(token)
        if not user_id:
            return {"error": error}, 401
        # Call the services
        code =  request.path.rstrip("/").split("/")[-1]
        if (code=="getchat"):
            send_to = request.headers.get("SendTo")
            if not send_to:
                return {"error": "Missing chat user"}, 400  
            service = ChatsService()
            return service.get_chat_messages(user_id, send_to)
        else:
            return {"error": f"Wrong code: {code}"}, 404

    def put(self): 
        """
        Puts new recado and new chat message
        """      
        code =  request.path.rstrip("/").split("/")[-1]
        user_id = None
        # Ckeck if the authorization header contains a jwt token
        auth_header = request.headers.get("Authorization")
        if auth_header and auth_header.startswith("Bearer "):
            token = auth_header.split(" ")[1]
        else:
            return {"error": "Missing authorization header"}, 401  
        # Check that jwt token is correct and extract the user.
        user_id, error = check_token(token)
        if not user_id:
            return {"error": error}, 401
        # Extract json body
        data = request.get_json()
        if not data:
            return {"error": "Request body is empty"}, 400
        # Calls service depending on the code    
        match code:
            case "recados":
                if "text" not in data or not data["text"].strip():
                    return {"error": "Text is required and cannot be empty"}, 400   
                service = RecadosService()
                return service.new_recado(user_id, data)            
            case "chats":
                text = data.get("text")
                if not text:
                    return {"error": "Text not found"}, 400
                send_to = data.get("send_to")
                if not send_to:
                    return {"error": "No recipient of the message found"}, 400                         
                service = ChatsService()
                return service.new_chat_message(user_id, text, send_to)
            case _:
                return {"error": f"Wrong code: {code}"}, 404
            
    def post(self):
        """
        For user login/register, token requests and update calls
        """    
        code =  request.path.rstrip("/").split("/")[-1]
        if(code == "login"):
            # Extract json body
            data = request.get_json()
            if not data:
                return {"error": "Request body is empty"}, 400  
            # Check email
            email = data.get("email")
            if not email:
                return {"error": "Email not found"}, 400
            password = data.get("password")
            if not password:
                return {"error": "Password not found"}, 400
            # Call service
            return UsersService().login_user(email, password)
        
        elif(code == "register"):
            # Extract json body
            data = request.get_json()
            if not data:
                return {"error": "Request body is empty"}, 400  
            # Check name
            name = data.get("name")
            if not name:
                return {"error": "Name not found"}, 400
            # Check email
            email = data.get("email")
            if not email:
                return {"error": "Email not found"}, 400
            password = data.get("password")
            if not password:
                return {"error": "Password not found"}, 400
            # Call service
            return UsersService().new_user(name, email, password)          
        
        elif(code == "token"):
            # Ckeck if the authorization header contains a jwt token
            auth_header = request.headers.get("Authorization")
            if auth_header and auth_header.startswith("Bearer "):
                token = auth_header.split(" ")[1]
                return {"token": refresh_token(token)}, 200
            else:
                return {"error": "Missing authorization header"}, 401  
        
        elif(code == "update"):
            # Ckeck if the authorization header contains a jwt token
            auth_header = request.headers.get("Authorization")
            if auth_header and auth_header.startswith("Bearer "):
                token = auth_header.split(" ")[1]
            else:
                return {"error": "Missing authorization header"}, 401    
            # Check that jwt token is correct and extract the user.
            user_id, error = check_token(token)
            if not user_id:
                return {"error": error}, 401
            # Extract json body
            data = request.get_json()
            if not data:
                return {"error": "Request body is empty"}, 400 
            # Update user location and activity
            UsersService().update_location(user_id, float(data["latitude"]), float(data["longitude"]))
            # Build response          
            updateData = {"recados": RecadosService().get_available_recados(user_id, data["map_radio"]),
                          "users": UsersService().get_available_users(user_id, data["map_radio"])}
            return updateData        
        else:
            return {"error": f"Wrong code: {code}"}, 404
