from django.db import models
from drf_spectacular.utils import extend_schema_serializer, OpenApiExample
from rest_framework import serializers
from onboardProject import settings


class Session(models.Model):
    session_id = models.IntegerField()
    city_address = models.CharField(max_length=255)
    date_time = models.CharField(max_length=255)
    name = models.CharField(max_length=255)
    owner = models.CharField(max_length=255, default=None)
    players = models.JSONField(default=None)
    players_max = models.IntegerField()

    def save_session(self, nickname, uid):
        self.owner = nickname
        self.players = [self.owner]
        settings.database.child(settings.SESSIONS_TABLE).child(self.session_id).update(SessionSerializer(self).data)
        user_sessions = settings.database.child(settings.USERS_TABLE).child(uid).child("played_sessions").get().val()
        if user_sessions:
            user_sessions += [self.session_id]
        else:
            user_sessions = [self.session_id]
        settings.database.child(settings.USERS_TABLE).child(uid).child('user_data').child('played_sessions')\
            .set(user_sessions)


@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Game session',
            summary="Game session template",
            value={
                'idToken': '...',
                'city_address': "Ул. Фридриха Энгельса, 24б, 2 этаж, Воронеж",
                'date_time': "2023.06.3 12:00",
                'name': "Кемет | ПараDice",
                'owner': 'Андрей Морозов',
                'players_max': 4
            },
            request_only=True,
            response_only=False
        )
    ]
)
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


@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Delete game session',
            summary="Delete",
            value={
                'idToken': '...'
            },
            request_only=True,
            response_only=False
        )
    ]
)
class SessionDeleteSerializer(serializers.Serializer):
    session_id = serializers.IntegerField()
    idToken = serializers.CharField(max_length=1024)
