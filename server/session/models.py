from django.db import models
from rest_framework import serializers
from onboardProject import settings
from firebase_tools.users_tools import get_user, get_user_token


class Session(models.Model):
    session_id = models.IntegerField()
    city_address = models.CharField(max_length=255)
    date_time = models.CharField(max_length=255)
    name = models.CharField(max_length=255)
    owner = models.CharField(max_length=255, default=None)
    players = models.JSONField(default=None)
    players_max = models.IntegerField()

    def save_session(self, login):
        session_info = {
            "owner": login,
            "players": [login]
        }
        settings.database.child("sessions").child(self.session_id).update(SessionSerializer(self).data)
        settings.database.child("sessions").child(self.session_id).update(session_info)
        user_sessions = settings.database.child("users").child(login).child("played_sessions").get().val()
        if user_sessions:
            user_sessions += [self.session_id]
        else:
            user_sessions = [self.session_id]
        settings.database.child("users").child(login).child("played_sessions").set(user_sessions)

    def add_reputation_to_players(self):
        players_info = {}
        for player_login in self.players:
            players_info[player_login] = {'reputation': dict(get_user(player_login).val()).get('reputation')}
        self.players = players_info

class SessionSerializer(serializers.Serializer):
    session_id = serializers.IntegerField()
    city_address = serializers.CharField(max_length=255)
    date_time = serializers.CharField(max_length=255)
    name = serializers.CharField(max_length=255)
    owner = serializers.CharField(max_length=255, default=None)
    players = serializers.JSONField(default=None)
    players_max = serializers.IntegerField()

    def create(self, validated_data):
        return Session(**validated_data)
