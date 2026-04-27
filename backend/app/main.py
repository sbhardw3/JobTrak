from fastapi import FastAPI
from .database import engine, Base
from .models import User
from .routes.auth import router as auth_router

Base.metadata.create_all(bind=engine)

app = FastAPI()

app.include_router(auth_router)

@app.get("/")
def root():
    return {"message": "JobTrak backend is running"}