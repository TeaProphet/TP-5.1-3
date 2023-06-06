from django.db import models
from drf_spectacular.utils import extend_schema_serializer, OpenApiExample
from rest_framework import serializers
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

    def register_session(self, nickname, uid):
        self.owner = nickname
        self.players = [nickname]
        session_info = SessionSerializer(self).data
        settings.database.child(settings.SESSIONS_TABLE).child(self.session_id).update(session_info)
        user_sessions = settings.database.child(settings.USERS_TABLE).child(uid).child('user_data').child('played_sessions').get().val()
        if user_sessions:
            user_sessions += [self.session_id]
        else:
            user_sessions = [self.session_id]
        settings.database.child(settings.USERS_TABLE).child(uid).child('user_data').child('played_sessions')\
            .set(user_sessions)


class SessionSerializer(serializers.Serializer):
    city_address = serializers.CharField(max_length=256)
    games = serializers.CharField(max_length=256)
    date_time = serializers.DateTimeField(format="%Y-%m-%d %H:%M")
    name = serializers.CharField(max_length=256)
    owner = serializers.CharField(max_length=256)
    players = serializers.JSONField(default=None)
    players_max = serializers.IntegerField()

    def create(self, validated_data):
        return Session(**validated_data)


@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Game session',
            summary="Game session template",
            value={
                'idToken': '...',
                'city_address': "Ул. Фридриха Энгельса, 24б, 2 этаж, Воронеж",
                'games': "Кемет",
                'date_time': "2023-06-03 12:00",
                'name': "Кемет | ПараDice",
                'players_max': 4
            },
            request_only=True
        )
    ]
)
class SessionRegistrationSerializer(serializers.Serializer):
    session_id = serializers.IntegerField()
    city_address = serializers.CharField(max_length=256)
    games = serializers.CharField(max_length=256)
    date_time = serializers.DateTimeField(format="%Y-%m-%d %H:%M")
    name = serializers.CharField(max_length=256)
    players_max = serializers.IntegerField()

    def create(self, validated_data):
        return Session(**validated_data)


class SessionPublicInfoSerializer(serializers.Serializer):
    city_address = serializers.CharField(max_length=256)
    games = serializers.CharField(max_length=256)
    date_time = serializers.CharField(max_length=256)
    name = serializers.CharField(max_length=256)
    players = serializers.JSONField()
    players_max = serializers.IntegerField()

    def create(self, validated_data):
        return Session(**validated_data)


class SessionPublicShortInfoSerializer(serializers.Serializer):
    city_address = serializers.CharField(max_length=256)
    date_time = serializers.CharField(max_length=256)
    name = serializers.CharField(max_length=256)
    players_max = serializers.IntegerField()

    def create(self, validated_data):
        return Session(**validated_data)



@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Session info',
            summary='Info',
            value=[
                {
                    "session_id": 0,
                    'city_address': "Ул. Фридриха Энгельса, 24б, 2 этаж, Воронеж",
                    'games': "Кемет",
                    'date_time': "2023.06.3 12:00",
                    'name': "Кемет | ПараDice",
                    'players_max': 4
                },
                {
                    "session_id": 1,
                    'city_address': "Ул. Фридриха Энгельса, 24б, 2 этаж, Воронеж",
                    'games': "DnD5",
                    'date_time': "2023.06.3 12:00",
                    'name': "DnD5 | ПараDice",
                    'players_max': 4
                }
            ],
            request_only=False,
            response_only=True
        )
    ]
)
class SessionsListSerializer(serializers.Serializer):
    session_id = serializers.IntegerField()

