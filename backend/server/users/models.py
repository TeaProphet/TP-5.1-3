from django.core.validators import MinLengthValidator, RegexValidator
from django.db import models
from drf_spectacular.utils import extend_schema_serializer, OpenApiExample
from rest_framework import serializers

import swagger_errors_examples.user_errors
import swagger_errors_examples.common_errors
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
            'Информация о профиле',
            summary="Информация о профиле",
            description="Вводимые значения:\n"
                        '1. vk - ссылка на страницу в соцсети "VK", должна начинаться с "vk.com/". Принимает значение null если не указана пользователем.\n'
                        '2. tg - ссылка на профиль в мессенджере "Telegram", должна начинаться с "t.me/". Принимает значение null если не указана пользователем.\n'
                        '3. age - возраст пользователя, может быть не более 100. Принимает значение null если не указан пользователем.\n'
                        '4. reputation - числовое значение репутации пользователя\n'
                        '5. player_sessions - массив sessionId, в которых принимал участие пользователь. Принимает значение null если таковых нет.\n'
                        '6. is_admin - статус администратора пользователя, задаётся напрямую в БД.\n'
                        '7. is_banned - статус блокировки пользователя.\n',
            value={
                'age': 20,
                'games': 'DnD5, Битвы героев',
                'vk': 'vk.com/id',
                'tg': 't.me/link',
            },
            request_only=True,
            response_only=False
        ),
        swagger_errors_examples.common_errors.invalid_data,
        swagger_errors_examples.user_errors.invalid_token,
        swagger_errors_examples.user_errors.expired_token
    ]
)
class ChangingUserDataSerializer(serializers.Serializer):
    age = serializers.IntegerField(allow_null=True)
    games = serializers.CharField(max_length=1024, allow_null=True)
    vk = serializers.CharField(
        max_length=64,
        allow_null=True,
        validators=[RegexValidator(r'^vk\.com\/.*$', message='Invalid VK link format')]
    )
    tg = serializers.CharField(
        max_length=64,
        allow_null=True,
        validators=[RegexValidator(r'^t\.me\/.*$', message='Invalid Telegram link format')]
    )

    def create(self, validated_data):
        return UserData(**validated_data)


class UserSerializer(serializers.Serializer):
    login = serializers.CharField(max_length=64, validators=[MinLengthValidator(3)], default=None)
    user_data = UserDataSerializer(default=UserData())

    def create(self, validated_data):
        return User(**validated_data)


@extend_schema_serializer(
    component_name='auth',
    examples=[
        OpenApiExample(
            'Информация для авторизации',
            summary='Данные для авторизации',
            description="Вводимые значения:\n"
                        "1. login - электронная почта пользователя.\n"
                        "2. password - не может быть менее 6 символов.",
            value={
                'login': "qwerty@yandex.ru",
                'password': "qwerty"
            },
            request_only=True,
            response_only=False
        ),
        OpenApiExample(
            'Данные авторизованного пользователя',
            summary='Данные авторизованного пользователя',
            description="Вовзращаемые значения:\n"
                        "1. nickname - никнейм авторизованного пользователя.\n"
                        "2. idToken - новый токен пользователя (старый при этом перестаёт действовать).",
            value={
                'nickname': 'qwerty',
                'idToken': '...'
            },
            request_only=False,
            response_only=True,
            status_codes=[200]
        ),
        swagger_errors_examples.user_errors.invalid_credentials,
        swagger_errors_examples.user_errors.email_not_exist,
        swagger_errors_examples.user_errors.weak_password,
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
            'Информация для регистрации',
            summary='Данные для регистрации',
            description="Вводимые значения:\n"
                        "1. login - электронная почта.\n"
                        "2. nickname - никнейм, минимум 4 символа.\n"
                        "3. password - пароль, минимум 6 символов.",
            value={
                'login': "qwerty@yandex.ru",
                'nickname': 'qwerty',
                'password': "qwerty"
            },
            request_only=True,
            response_only=False
        ),
        OpenApiExample(
            'Данные зарегистрированного пользователя',
            description="Возвращается idToken пользователя, дающий доступ к его профилю и функциям.",
            summary='Данные зарегистрированного пользователя',
            value={
                'idToken': "...",
            },
            request_only=False,
            response_only=True
        ),
        swagger_errors_examples.user_errors.invalid_credentials,
        swagger_errors_examples.user_errors.invalid_email,
        swagger_errors_examples.user_errors.email_exists,
        swagger_errors_examples.user_errors.nickname_exists,
        swagger_errors_examples.user_errors.weak_password
    ]
)
class RegistrationSerializer(serializers.Serializer):
    login = serializers.CharField(max_length=64, validators=[MinLengthValidator(3)], default=None)
    nickname = serializers.CharField(max_length=64, default=None)

    def create(self, validated_data):
        return User(**validated_data)


@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Ответ с пользовательскими данными',
            summary='Информация о пользователе',
            description="Возвращаются поля:\n"
                        '1. vk - ссылка на страницу в соцсети "VK", должна начинаться с "vk.com/". Принимает значение null если не указана пользователем.\n'
                        '2. tg - ссылка на профиль в мессенджере "Telegram", должна начинаться с "t.me/". Принимает значение null если не указана пользователем.\n'
                        '3. age - возраст пользователя, может быть не более 100. Принимает значение null если не указан пользователем.\n'
                        '4. reputation - числовое значение репутации пользователя.\n'
                        '5. games - список любимых игр пользователя. Принимает значение null если не указаны пользователем\n'
                        '6. player_sessions - массив sessionId, в которых принимал участие пользователь. Принимает значение null если таковых нет.\n'
                        '7. is_admin - статус администратора пользователя, задаётся напрямую в БД.\n'
                        '8. is_banned - статус блокировки пользователя.\n',
            value={
                'vk': "vk.com/test",
                'tg': "t.me/test",
                'age': "256",
                'reputation': "128",
                'games': "...",
                'player_sessions': "[..., ...]",
                'is_admin': 'false',
                'is_banned': 'false'
            },
            request_only=False,
            response_only=True,
            status_codes=[200]
        ),
        swagger_errors_examples.common_errors.invalid_data
    ]
)
class SearchedNicknameSerializer(serializers.Serializer):
    nickname = serializers.CharField(max_length=64, validators=[MinLengthValidator(3)], default=None)


@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Запрос для увеличения репутации другого пользователя.',
            summary="Запрос увеличения репутации.",
            description="Вводимые значения:\n"
                        "1. requestedNickname - никнейм пользователя, которому нужно увеличить репутацию.\n"
                        "2. idToken - токен пользователя, который запросил увеличение репутации.",
            value={
                'requestedNickname': 'testrep',
                'idToken': '...'
            },
            request_only=True,
            response_only=False
        ),
        OpenApiExample(
            'Ответ с изменённой репутацией.',
            summary="Успешно изменённая репутация",
            description="Выводится изменённое значение репутации.",
            value={
                'new_reputation': '...'
            },
            request_only=False,
            response_only=True
        ),
        swagger_errors_examples.user_errors.access_denied,
        swagger_errors_examples.user_errors.already_plused,
        swagger_errors_examples.user_errors.expired_token,
        swagger_errors_examples.user_errors.invalid_token
    ]
)
class PlusReputationRequestSerializer(serializers.Serializer):
    requestedNickname = serializers.CharField(max_length=64)
    idToken = serializers.CharField(max_length=1024)


@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Запрос для уменьшения репутации другого пользователя.',
            summary="Запрос уменьшения репутации.",
            description="Вводимые значения:\n"
                        "1. requestedNickname - никнейм пользователя, которому нужно уменьшить репутацию.\n"
                        "2. idToken - токен пользователя, который запросил уменьшение репутации.",
            value={
                'requestedNickname': 'testrep',
                'idToken': '...'
            },
            request_only=True,
            response_only=False
        ),
        OpenApiExample(
            'Ответ с изменённой репутацией.',
            summary="Успешно изменённая репутация",
            description="Выводится изменённое значение репутации.",
            value={
                'new_reputation': '...'
            },
            request_only=False,
            response_only=True
        ),
        swagger_errors_examples.user_errors.access_denied,
        swagger_errors_examples.user_errors.already_plused,
        swagger_errors_examples.user_errors.expired_token,
        swagger_errors_examples.user_errors.invalid_token
    ]
)
class MinusReputationRequestSerializer(serializers.Serializer):
    requestedNickname = serializers.CharField(max_length=64)
    idToken = serializers.CharField(max_length=1024)


@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Запрос для блокировки другого пользователя.',
            summary="Запрос блокировки",
            description="Вводимые значения:\n"
                        "1. requestedNickname - никнейм пользователя, которого нужно заблокировать.\n"
                        "2. idToken - токен пользователя, который запросил блокировку.",
            value={
                'requestedNickname': 'testrep',
                'idToken': '...'
            },
            request_only=True,
            response_only=False
        ),
        swagger_errors_examples.user_errors.access_denied,
        swagger_errors_examples.user_errors.already_banned,
        swagger_errors_examples.user_errors.expired_token,
        swagger_errors_examples.user_errors.invalid_token
    ]
)
class BanRequestSerializer(serializers.Serializer):
    requestedNickname = serializers.CharField(max_length=64)
    idToken = serializers.CharField(max_length=1024)


@extend_schema_serializer(
    examples=[
        OpenApiExample(
            'Запрос для разблокировки другого пользователя',
            summary="Запрос разблокировки.",
            description="Вводимые значения:\n"
                        "1. requestedNickname - никнейм пользователя, которого нужно разблокировать.\n"
                        "2. idToken - токен пользователя, который запросил разблокировку.",
            value={
                'requestedNickname': 'testrep',
                'idToken': '...'
            },
            request_only=True,
            response_only=False
        ),
        swagger_errors_examples.user_errors.already_unbanned,
        swagger_errors_examples.user_errors.access_denied,
        swagger_errors_examples.user_errors.expired_token,
        swagger_errors_examples.user_errors.invalid_token
    ]
)
class UnbanRequestSerializer(serializers.Serializer):
    requestedNickname = serializers.CharField(max_length=64)
    idToken = serializers.CharField(max_length=1024)
