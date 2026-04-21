from fastapi import FastAPI
from .database import engine, Base
from .models import User

Base.metadata.create_all(bind=engine)

app = FastAPI()

@app.get("/")
def root():
    return {"message": "JobTrak backend is running"}