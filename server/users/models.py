from django.core.validators import MinLengthValidator
from django.db import models
from drf_spectacular.utils import extend_schema_serializer, OpenApiExample
from rest_framework import serializers

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

    def save_data(self):
        user_data = dict(UserDataSerializer(self.user_data).data)
        if len(settings.database.child(settings.USERS_TABLE).order_by_child('nickname').equal_to(self.nickname).get().each()) > 0:
            raise ValueError('NICKNAME_EXISTS')
        settings.database.child(settings.USERS_TABLE).child(self.uid).child('user_data').update(user_data)
        settings.database.child(settings.USERS_TABLE).child(self.uid).child('login').set(self.login)
        settings.database.child(settings.USERS_TABLE).child(self.uid).child('nickname').set(self.nickname)


class UserDataSerializer(serializers.Serializer):
    reputation = serializers.IntegerField(default=0)
    age = serializers.IntegerField(default=None)
    games = serializers.CharField(max_length=1024, default=None)
    vk = serializers.CharField(max_length=64, default=None)
    tg = serializers.CharField(max_length=64, default=None)
    played_sessions = serializers.JSONField(default=None)
    is_admin = serializers.BooleanField(default=False)
    is_banned = serializers.BooleanField(default=False)

    def create(self, validated_data):
        return UserData(**validated_data)

@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Profile info',
            summary="Profile info template",
            value={
                'age': 20,
                'games': 'DnD5, Битвы героев',
                'vk': 'vk.com/id',
                'tg': 't.me/link',
            }
        )
    ]
)
class ChangingUserDataSerializer(serializers.Serializer):
    age = serializers.IntegerField(allow_null=True)
    games = serializers.CharField(max_length=1024, allow_null=True)
    vk = serializers.CharField(max_length=64, allow_null=True)
    tg = serializers.CharField(max_length=64, allow_null=True)

    def create(self, validated_data):
        return UserData(**validated_data)


class UserSerializer(serializers.Serializer):
    login = serializers.CharField(max_length=64, validators=[MinLengthValidator(3)], default=None)
    user_data = UserDataSerializer(default=UserData())

    def create(self, validated_data):
        return User(**validated_data)


@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Reg data',
            summary='Reg',
            description="Used values:\n"
                        "1. login - email-address of user.\n"
                        "2. nickname - nickname of user, can' be less than 4 symbols.\n"
                        "3. password - can't be less than 6 symbols.",
            value={
                'login': "qwerty@yandex.ru",
                'nickname': 'qwerty',
                'password': "qwerty"
            },
            request_only=True,
            response_only=False
        ),
        OpenApiExample(
            'Response with idToken',
            summary='Returns idToken of user',
            value={
                'idToken': "...",
            },
            request_only=False,
            response_only=True
        )
    ]
)
class RegistrationSerializer(serializers.Serializer):
    login = serializers.CharField(max_length=64, validators=[MinLengthValidator(3)], default=None)
    nickname = serializers.CharField(max_length=64, default=None)

    def create(self, validated_data):
        return User(**validated_data)


@extend_schema_serializer(
    component_name='auth',
    examples=[
        OpenApiExample(
            'Auth data',
            summary='Auth',
            description="Used values:\n"
                        "1. login - email-address of user.\n"
                        "2. password - can't be less than 6 symbols.",
            value={
                'login': "qwerty@yandex.ru",
                'password': "qwerty"
            },
            request_only=True,
            response_only=False
        ),
        OpenApiExample(
            'Returns idToken and nickname of user',
            summary='Auth response',
            value={
                'nickname': 'qwerty',
                'idToken': '...'
            },
            request_only=False,
            response_only=True
        )
    ]
)
class AuthorizationSerializer(serializers.Serializer):
    login = serializers.CharField(max_length=64, default=None)
    password = serializers.CharField(max_length=64, validators=[MinLengthValidator(6)], default=None)

    def create(self, validated_data):
        return User(**validated_data)


@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Response with user data',
            summary='Returns data of user',
            value={
                'vk': "vk.com/test",
                'tg': "t.me/test",
                'age': "256",
                'reputation': "128",
                'player_sessions': "[..., ...]",
                'is_admin': 'false',
                'is_banned': 'false'
            },
            request_only=False,
            response_only=True
        )
    ]
)
class SearchedNicknameSerializer(serializers.Serializer):
    nickname = serializers.CharField(max_length=64, validators=[MinLengthValidator(3)], default=None)

@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Request for access to change profile info (reputation/ban/unban)',
            summary="Access request",
            value={
                'requestedNickname': 'testrep',
                'idToken': '...'
            },
            request_only=True,
            response_only=False
        )
    ]
)
class AccessToProfileRequestSerializer(serializers.Serializer):
    requestedNickname = serializers.CharField(max_length=64)
    idToken = serializers.CharField(max_length=1024)