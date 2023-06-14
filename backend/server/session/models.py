from django.db import models
from onboardProject import settings


class Session(models.Model):
    session_id = models.IntegerField()
    city_address = models.CharField(max_length=256)
    games = models.CharField(max_length=256)
    date_time = models.DateTimeField()
    name = models.CharField(max_length=256)
    owner = models.CharField(max_length=256)
    players = models.JSONField(default=None)
    players_max = models.IntegerField()
