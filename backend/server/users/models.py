from django.db import models
from onboardProject import settings


class UserData(models.Model):
    reputation = models.IntegerField(default=0)
    age = models.IntegerField(default=None)
    games = models.TextField(max_length=1024, default=None)
    vk = models.CharField(max_length=64, default=None)
    tg = models.CharField(max_length=64, default=None)
    played_sessions = models.JSONField(default=None)
    is_admin = models.BooleanField(default=False)
    is_banned = models.BooleanField(default=False)


class User(models.Model):
    uid = models.CharField(max_length=1024, default=None)
    login = models.CharField(max_length=64)
    nickname = models.CharField(max_length=64)
    user_data = models.OneToOneField(UserData, on_delete=models.CASCADE, default=UserData())
