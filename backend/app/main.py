from fastapi import FastAPI
from .database import engine, Base

app = FastAPI()

@app.get("/")
def root():
    return {"message": "JobTrak backend is running"}