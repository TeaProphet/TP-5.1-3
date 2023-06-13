from django.db import models
from drf_spectacular.utils import extend_schema_serializer, OpenApiExample
from rest_framework import serializers

import swagger_errors_examples.user_errors
import swagger_errors_examples.common_errors
import swagger_errors_examples.session_errors
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
            'Создание игровой сессии',
            summary="Создание игровой сессии",
            description="Вводимые значения:\n"
                        "1. idToken - токен создателя сессии.\n"
                        "2. city_address - адрес проведения игровой сессии (валидации нет).\n"
                        "3. games - игры, в которые будут/могут сыграть пользователи на игровой сессии.\n"
                        "4. date_time - время проведения игровой сессии формата %Y-%m-%d %H:%M.\n"
                        "5. name - название игровой сессии.\n"
                        "6. players_max - максимальное число участников игровой сессии.\n",
            value={
                'idToken': '...',
                'city_address': "Ул. Фридриха Энгельса, 24б, 2 этаж, Воронеж",
                'games': "Кемет",
                'date_time': "2023-06-03 12:00",
                'name': "Кемет | ПараDice",
                'players_max': 4
            },
            request_only=True,
        ),
        swagger_errors_examples.common_errors.invalid_data,
        swagger_errors_examples.user_errors.invalid_token,
        swagger_errors_examples.user_errors.expired_token
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



@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Информация об игровой сессии',
            summary="Информация об игровой сессии",
            description="Выводимые значения:\n"
                        "1. city_address - адрес проведения игровой сессии (валидации нет).\n"
                        "2. games - игры, в которые будут/могут сыграть пользователи на игровой сессии.\n"
                        "3. date_time - время проведения игровой сессии формата %Y-%m-%d %H:%M.\n"
                        "4. name - название игровой сессии.\n"
                        "5. owner - владелец игровой сессии.\n"
                        "6. players - массив никнеймов участников игровой сессии.\n"
                        "6. players_max - максимальное число участников игровой сессии.",
            value={
                'city_address': "Ул. Фридриха Энгельса, 24б, 2 этаж, Воронеж",
                'games': "Кемет",
                'date_time': "2023-06-03 12:00",
                'name': "Кемет | ПараDice",
                "owner": "nickname",
                'players': ["nickname", "nickname", "..."],
                'players_max': 4
            },
            response_only=True
        ),
        swagger_errors_examples.common_errors.invalid_data
    ]
)
class SessionPublicInfoSerializer(serializers.Serializer):
    city_address = serializers.CharField(max_length=256)
    games = serializers.CharField(max_length=256)
    date_time = serializers.CharField(max_length=256)
    name = serializers.CharField(max_length=256)
    owner = serializers.CharField(max_length=256)
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
            'Список игровых сессий',
            summary='Список игровых сессий',
            description="Выводит массив информации об игровых сессиях. Каждый элемент массива включает в себя:\n"
                        "1. session_id - идентификатор игровой сессии.\n"
                        "2. city_address - адрес проведения игровой сессии (валидации нет).\n"
                        "3. games - игры, в которые будут/могут сыграть пользователи на игровой сессии.\n"
                        "4. date_time - время проведения игровой сессии формата %Y-%m-%d %H:%M.\n"
                        "5. name - название игровой сессии.\n"
                        "6. players_max - максимальное число участников игровой сессии.",
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


@extend_schema_serializer(examples=[
    swagger_errors_examples.common_errors.invalid_data,
    swagger_errors_examples.user_errors.access_denied,
    swagger_errors_examples.session_errors.session_not_found,
    swagger_errors_examples.user_errors.expired_token,
    swagger_errors_examples.user_errors.invalid_token
])
class SessionDeleteSerializer(serializers.Serializer):
    idToken = serializers.IntegerField()
    session_id = serializers.IntegerField()


@extend_schema_serializer(examples=[
    swagger_errors_examples.common_errors.invalid_data,
    swagger_errors_examples.session_errors.session_not_found,
    swagger_errors_examples.session_errors.already_joined,
    swagger_errors_examples.user_errors.expired_token,
    swagger_errors_examples.user_errors.invalid_token
])
class SessionJoinSerializer(serializers.Serializer):
    idToken = serializers.IntegerField()
    session_id = serializers.IntegerField()


@extend_schema_serializer(examples=[
    swagger_errors_examples.common_errors.invalid_data,
    swagger_errors_examples.session_errors.session_not_found,
    swagger_errors_examples.session_errors.already_left,
    swagger_errors_examples.user_errors.expired_token,
    swagger_errors_examples.user_errors.invalid_token
])
class SessionLeftSerializer(serializers.Serializer):
    idToken = serializers.IntegerField()
    session_id = serializers.IntegerField()


@extend_schema_serializer(examples=[
    swagger_errors_examples.common_errors.invalid_data,
    swagger_errors_examples.session_errors.session_not_found,
    swagger_errors_examples.user_errors.access_denied,
    swagger_errors_examples.user_errors.expired_token,
    swagger_errors_examples.user_errors.invalid_token
])
class ChangeSessionNameSerializer(serializers.Serializer):
    idToken = serializers.IntegerField()
    new_name = serializers.CharField(max_length=256)
    session_id = serializers.IntegerField()

