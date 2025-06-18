from src import create_app
from db import create_db

# FOR DEPLOYMENT
# import os

if __name__ == "__main__":
    # FOR LOCAL TESTS mockupdb
    db = create_db()

    # Create instance and run Flask-Restful
    app = create_app()

    # FOR LOCAL TESTS (run Flask)
    app.run(
        host="0.0.0.0", 
        port=5000, 
        debug=True,
        # Ssl self cerfificate for testing
        ssl_context="adhoc"
    )

    # FOR DEPLOYMENT (run Flask)
    # port = int(os.environ.get("PORT", 5000))
    # app.run(host="0.0.0.0", port=port)


    